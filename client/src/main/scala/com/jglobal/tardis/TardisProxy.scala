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

object TardisProxy {
  def apply(clientId: String, busHostAndPort: String = "127.0.0.1:9999") = {
    val system = ActorSystem("client", ConfigFactory.load("client"))
    val address = s"akka.tcp://application@$busHostAndPort/user/eventrouter"
    val busSelection = system.actorSelection(address)
    try {
      implicit val timeout = Timeout(10 seconds)
      val future: Future[ActorIdentity] = ask(busSelection, Identify).mapTo[ActorIdentity]
      val busIdentity = Await.result(future, 10 seconds).asInstanceOf[ActorIdentity]
      val bus = busIdentity.ref.getOrElse(throw new RuntimeException(s"Can't contact the bus at $address"))
      val proxyActor = system.actorOf(TardisProxyActor.props(bus), "busProxy")
      new TardisProxy(clientId, bus, proxyActor)
    } catch {
      case ate: AskTimeoutException => throw new RuntimeException(s"Cannot connect to tardis server at $address, timed out")
      case toe: TimeoutException => throw new RuntimeException(s"Cannot connect to tardis server at $address, timed out")
    }
  }
}

class TardisProxy(val clientId: String, bus: ActorRef, proxyActor: ActorRef) {

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    proxyActor ! SendEvent(evt, confirm)
  }
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    proxyActor ! HandlerRegistered(handler, Subscription(clientId, List(eventType)))
  }
  
  def ack(id: UUID) {
    proxyActor ! SendAck(Ack(id, clientId))
  }
}

object TardisProxyActor {
  def props(bus: ActorRef): Props = Props(classOf[TardisProxyActor], bus)
}

case class SendEvent(container: EventContainer, confirm: (Ack) => Unit)
case class SendAck(ack: Ack)
case class HandlerRegistered(handler: (EventContainer) => Unit, subscription: Subscription)

class TardisProxyActor(bus: ActorRef) extends Actor with ActorLogging {
  val pending = new collection.mutable.HashMap[UUID, (Ack) => Unit] with SynchronizedMap[UUID, (Ack) => Unit]
  val handlers = new collection.mutable.HashMap[String, (EventContainer) => Unit] with SynchronizedMap[String, (EventContainer) => Unit]
  
  def receive = {
    case SendEvent(container, confirm) => {
      pending.put(container.id, confirm)
      bus ! container
    }

    case SendAck(ack) => bus ! ack

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
      pending.get(ack.id) match {
        case Some(f) => {
          f(ack)
          pending.remove(ack.id)
        }
        case None => {
          log.error(s"Received an ack for id ${ack.id} which we do not have a pending handler for")
        }
      }
    }
  }
}





