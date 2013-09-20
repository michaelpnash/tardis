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
  // def ping(busHostAndPort: String = "127.0.0.1:9999"): Boolean = {
  //   val system = ActorSystem("clientPing", ConfigFactory.load("client"))
  //   val address = s"akka.tcp://application@$busHostAndPort/user/eventrouter"
  //   val busSelection = system.actorSelection(address)
  //   val result = {
  //     try {
  //       implicit val timeout = Timeout(30 seconds)
  //       val future: Future[ActorIdentity] = ask(busSelection, Identify).mapTo[ActorIdentity]
  //       val busIdentity = Await.result(future, 30 seconds).asInstanceOf[ActorIdentity]
  //       busIdentity.ref.isDefined
  //     } catch {
  //       case _: Throwable => false
  //     }
  //     true
  //   }
  //   system.shutdown
  //   result
  // }
  def apply(clientId: String, busHostAndPort: String = "127.0.0.1:9999") = {
    val system = ActorSystem("client", ConfigFactory.load("client"))
    val address = s"akka.tcp://application@$busHostAndPort/user/eventrouter"
    val busSelection = system.actorSelection(address)
    //assert(ping(address), s"Cannot connect to tardis server at $address")
    val proxyActor = system.actorOf(TardisProxyActor.props(busSelection), "busProxy")
    new TardisProxy(clientId, proxyActor, system)
  }
}

class TardisProxy(val clientId: String, proxyActor: ActorRef, system: ActorSystem) {

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    proxyActor ! SendEvent(evt, confirm)
  }
  
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

case class SendEvent(container: EventContainer, confirm: (Ack) => Unit)
case class SendAck(ack: Ack)
case class HandlerRegistered(handler: (EventContainer) => Unit, subscription: Subscription)

class TardisProxyActor(bus: ActorSelection) extends Actor with ActorLogging {
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





