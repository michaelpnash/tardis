package com.jglobal.tardis

import java.util.UUID

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException

import akka.actor._
import com.typesafe.config._

class TardisProxy(val clientId: String, proxyActor: ActorRef, system: ActorSystem) {

  def this(clientId: String, busHostAndPort: String, system: ActorSystem) =
    this(clientId, system.actorOf(TardisProxyActor.props(system.actorSelection(s"akka.tcp://application@$busHostAndPort/user/eventrouter"))), system)

  def this(clientId: String, busHostAndPort: String = "127.0.0.1:9999") = this(clientId, busHostAndPort, ActorSystem("client", ConfigFactory.load("client")))

  def ping: Boolean = {
    val result = {
      try {
        implicit val timeout = Timeout(30 seconds)
        val future: Future[Boolean] =
          ask(proxyActor, Ping).mapTo[Boolean]
        Await.result(future, 30 seconds).asInstanceOf[Boolean]
      } catch {
        case _: Throwable => false
      }
    }
    result
  }

  def stats(id: String = clientId): ClientStats = {
    implicit val timeout = Timeout(30 seconds)
    val future: Future[ClientStats] = ask(proxyActor, Stats(id)).mapTo[ClientStats]
    Await.result(future, 30 seconds).asInstanceOf[ClientStats]
  }

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    require(evt.clientId == clientId)
    proxyActor ! SendEvent(evt, confirm)
  }

  def publish(eventType: String, payload: String, confirm: (Ack) => Unit) {
    publish(EventContainer(UUID.randomUUID, eventType, payload, clientId), confirm)
  }

  //TODO: publishAndWait
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    proxyActor ! HandlerRegistered(handler, Subscription(clientId, List(eventType)))
  }
  
  def ack(id: UUID) {
    proxyActor ! SendAck(Ack(id, clientId))
  }

  def shutdown {
    system.shutdown
  }
}

object TardisProxyActor {
  def props(bus: ActorSelection): Props = Props(classOf[TardisProxyActor], bus)
}

case class Stats(clientId: String)
case object Ping
case class SendEvent(container: EventContainer, confirm: (Ack) => Unit)
case class SendAck(ack: Ack)
case class HandlerRegistered(handler: (EventContainer) => Unit, subscription: Subscription)
case class RetrySend(eventId: UUID)

class TardisProxyActor(bus: ActorSelection) extends Actor with ActorLogging {
  val sentEventsPendingAck = new collection.mutable.HashMap[UUID, SendEvent] with SynchronizedMap[UUID, SendEvent]
  val handlers = new collection.mutable.HashMap[String, (EventContainer) => Unit] with SynchronizedMap[String, (EventContainer) => Unit]

  var unmatchedAcks = 0
  
  implicit val timeout = Timeout(30 seconds)
  
  def receive = {
    case send: SendEvent => {
      sentEventsPendingAck.put(send.container.id, send)
      bus ! send.container
      context.system.scheduler.scheduleOnce(30 seconds, self, RetrySend(send.container.id))(context.dispatcher)
    }

    case RetrySend(id: UUID) => sentEventsPendingAck.get(id) match {
      case Some(send) => {
        println(s"Retrying send of event $send, no ack received yet")
        self ! send
      }
      case None => {}
    }

    case Stats(clientId) => {
      val future: Future[ClientStats] = ask(bus, ClientStats(clientId)).mapTo[ClientStats]
      sender ! Await.result(future, 30 seconds).asInstanceOf[ClientStats]
    }

    case SendAck(ack) => bus ! ack

    case Ping => {
      try {
        val future: Future[ActorIdentity] = ask(bus, Identify).mapTo[ActorIdentity]
        val busIdentity = Await.result(future, 30 seconds).asInstanceOf[ActorIdentity]
        sender ! busIdentity.ref.isDefined
      } catch {
        case _: Throwable => sender ! false
      }
    }

    case container: EventContainer => handlers.get(container.eventType).map(f => f(container))

    case handlerRegistered: HandlerRegistered => {
      handlers.put(handlerRegistered.subscription.eventTypes.head, handlerRegistered.handler)
      self ! handlerRegistered.subscription
    }

    case subscription: Subscription => {
      bus ! Subscription(subscription.clientId, handlers.keys.toList)
      context.system.scheduler.scheduleOnce(15 seconds, self, subscription)(context.dispatcher)
    }
      
    case ack: Ack => {
      sentEventsPendingAck.get(ack.id) match {
        case Some(f) => {
          f.confirm(ack)
          sentEventsPendingAck.remove(ack.id)
        }
        case None => {
          unmatchedAcks += 1
          log.error(s"Received an ack for id ${ack.id} which we do not have a pending handler for (got $unmatchedAcks of these)")

        }
      }
    }
  }
}





