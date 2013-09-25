package com.jglobal.tardis

import org.scalatest.{FreeSpec, BeforeAndAfter, BeforeAndAfterAll}
import akka.actor._
import com.typesafe.config._
import akka.testkit._
import collection.mutable.ListBuffer
import java.util.UUID

class TardisProxyTest(system: ActorSystem) extends TestKit(system) with FreeSpec with BeforeAndAfter with BeforeAndAfterAll {
  def this() = this(ActorSystem("test-proxy", ConfigFactory.load("client").getConfig("test")))

  implicit val implSys = system
  val received = collection.mutable.ListBuffer[Any]()
  before {
    received.clear
  }

  val busStub = system.actorOf(TestActor.props(received))
  val busStubSelection = system.actorSelection(busStub.path)
  
  "the tardis proxy" - {
    "when a handler is registered, add the type to the list of subscribed types sent to the server" in {
      val proxy = new TardisProxy("clientId", system.actorOf(TardisProxyActor.props(busStubSelection)), system)
      val eventType = "foo"
      proxy.registerHandler((e: EventContainer) => {}, eventType)
      awaitCond(received.size == 1)
    }
    "when publishing an event, sends the event to the server" in {
      val clientId = "clientId"
      val proxy = new TardisProxy(clientId, system.actorOf(TardisProxyActor.props(busStubSelection)), system)
      val eventType = "foo"
      val event = EventContainer(UUID.randomUUID, eventType, "payload", clientId)
      val assertAck = { ack: Ack => assert(ack.id === event.id) }
      proxy.publish(event, assertAck)
      awaitCond(received.contains(event))
    }
    "when receiving an ack from the server, sends it to the confirmation function" in {
      val clientId = "clientId"
      val proxy = new TardisProxy(clientId, system.actorOf(TardisProxyActor.props(busStubSelection)), system)
      val eventType = "foo"
      val event = EventContainer(UUID.randomUUID, eventType, "payload", clientId)
      var receivedAck: Option[Ack] = None
      val assertAck = { ack: Ack => receivedAck = Some(ack) }
      proxy.publish(event, assertAck)
      awaitCond(receivedAck.isDefined)
      assert(receivedAck.get.id === event.id)
    }
    "when receiving an event from the server, sends it to the registered handler" in {
      val clientId = "clientId"
      val proxyActor = system.actorOf(TardisProxyActor.props(busStubSelection))
      val proxy = new TardisProxy(clientId, proxyActor, system)
      val eventType = "foo"
      val event = EventContainer(UUID.randomUUID, eventType, "payload", clientId)
      var receivedEvent: Option[EventContainer] = None
      val handler = { evt: EventContainer => receivedEvent = Some(evt) }
      proxy.registerHandler(handler, eventType)
      proxyActor ! event
      awaitCond(receivedEvent.isDefined)
      assert(receivedEvent.get === event)      
    }
    "when given an ack, sends it to the server" in {
      val clientId = "clientId"
      val proxy = new TardisProxy(clientId, system.actorOf(TardisProxyActor.props(busStubSelection)), system)
      val id = UUID.randomUUID
      proxy.ack(id)
      awaitCond(received.toList == List(Ack(id, clientId)))
    }
  }

  override def afterAll() {
    system.shutdown
  }
}

object TestActor {
  def props(received: ListBuffer[Any]) = Props(classOf[TestActor], received)
}

class TestActor(received: ListBuffer[Any]) extends Actor {
  def receive = {
    case evt: EventContainer => {
      received.append(evt)
      sender ! Ack(evt.id, evt.clientId)
    }
    case x: Any => received.append(x)
  }
}
