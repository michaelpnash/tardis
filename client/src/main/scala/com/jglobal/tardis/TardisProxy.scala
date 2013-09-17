package com.jglobal.tardis

import java.util.UUID

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

import akka.actor._
import com.typesafe.config._

object TardisProxy {
  def apply(clientId: String) = {
    val system = ActorSystem("client", ConfigFactory.load("client"))
    val bus = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
    val proxyActor = system.actorOf(TardisProxyActor.props(bus), "busProxy")
    new TardisProxy(clientId, bus, proxyActor)
  }
}

class TardisProxy(clientId: String, bus: ActorSelection, proxyActor: ActorRef) {

   val handlers = new collection.mutable.HashMap[String, (EventContainer) => Unit] with SynchronizedMap[String, (EventContainer) => Unit]

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    proxyActor ! SendEvent(evt)
  }
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    handlers.put(eventType, handler)
    proxyActor ! Subscription(clientId, handlers.keys.toList)
  }
  def ack(id: UUID) {}
}

object TardisProxyActor {
  def props(bus: ActorSelection): Props = Props(classOf[TardisProxyActor], bus)
}

case class SendEvent(container: EventContainer)

class TardisProxyActor(bus: ActorSelection) extends Actor with ActorLogging {
  def receive = {
    case SendEvent(container) => bus ! container
    case _ => {}
  }
}
