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
    implicit val timeout = Timeout(5 seconds)
    val future: Future[ActorIdentity] = ask(busSelection, Identify).mapTo[ActorIdentity]
    val busIdentity = Await.result(future, 3 seconds).asInstanceOf[ActorIdentity]
    val bus = busIdentity.ref.getOrElse(throw new RuntimeException("Can't contact the bus"))
    val proxyActor = system.actorOf(TardisProxyActor.props(bus), "busProxy")
    new TardisProxy(clientId, bus, proxyActor)
  }
}

class TardisProxy(clientId: String, bus: ActorRef, proxyActor: ActorRef) {

   val handlers = new collection.mutable.HashMap[String, (EventContainer) => Unit] with SynchronizedMap[String, (EventContainer) => Unit]

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {
    proxyActor ! SendEvent(evt, confirm)
  }
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    handlers.put(eventType, handler)
    println(s"Sending a list of ${handlers.size} types to subscription")
    proxyActor ! Subscription(clientId, handlers.keys.toList)
  }
  def ack(id: UUID) {}
}

object TardisProxyActor {
  def props(bus: ActorRef): Props = Props(classOf[TardisProxyActor], bus)
}

case class SendEvent(container: EventContainer, confirm: (Ack) => Unit)

class TardisProxyActor(bus: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case SendEvent(container, confirm) => {
      println("*" * 10 + "Sending event to bus")
      bus ! container
    }
    case subscription: Subscription => {
      println(s"Sending subscription $subscription to ${bus}")
      bus ! subscription
    }
    case _ => {}
  }
}
