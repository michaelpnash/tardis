package com.jglobal.tardis

import java.util.UUID

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

import akka.actor._
import com.typesafe.config._

object TardisProxy {
  def apply(clientId: String) = {
    val system = ActorSystem("client", ConfigFactory.load("client"))
    val busSelection = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
    implicit val timeout = Timeout(10 seconds)
    val future: Future[ActorIdentity] = ask(busSelection, Identify).mapTo[ActorIdentity]
    val busIdentity = Await.result(future, 10 seconds).asInstanceOf[ActorIdentity]
    val bus = busIdentity.ref.getOrElse(throw new RuntimeException("Can't contact the bus"))
    val proxyActor = system.actorOf(TardisProxyActor.props(bus), "busProxy")
    new TardisProxy(clientId, bus, proxyActor)
  }
}

class TardisProxy(clientId: String, bus: ActorRef, proxyActor: ActorRef) {

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    proxyActor ! SendEvent(evt, confirm)
  }
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    proxyActor ! HandlerRegistered(handler, Subscription(clientId, List(eventType)))
  }
  
  def ack(id: UUID) {
    proxyActor ! SendAck(Ack(id))
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
    case subscription: Subscription => bus ! subscription

    case SendAck(ack) => bus ! ack

    case container: EventContainer => {
      println(s"Got an event container in proxy actor $container")
      handlers.get(container.eventType).map(f => f(container))
    }

    case handlerRegistered: HandlerRegistered => {
      handlers.put(handlerRegistered.subscription.eventTypes.head, handlerRegistered.handler)
      bus ! Subscription(handlerRegistered.subscription.clientId, handlers.keys.toList)
    }
    case ack: Ack => {
      pending.get(ack.id) match {
        case Some(f) => {
          f(ack)
          pending.remove(ack.id)
        }
        case None => {} // warning?
      }
    }
  }
}





