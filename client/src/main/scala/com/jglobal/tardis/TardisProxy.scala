package com.jglobal.tardis

import java.util.UUID

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

import akka.actor._

class TardisProxy(bus: ActorRef) {
  val handlers = new collection.mutable.HashMap[String, (EventContainer) => Unit] with SynchronizedMap[String, (EventContainer) => Unit]

  def publish(evt: EventContainer, confirm: (Ack) => Unit) {}
  
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {
    handlers.put(eventType, handler)
  }
  def ack(id: UUID) {}
}

class TardisProxyActor(bus: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case _ => {}
  }
}
