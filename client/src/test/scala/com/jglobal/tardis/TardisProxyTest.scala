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
  
  "the tardis proxy" - {
    "when a handler is registered, add the type to the list of subscribed types sent to the server" in {
      val proxy = new TardisProxy("clientId", busStub, system.actorOf(TardisProxyActor.props(busStub)))
      val eventType = "foo"
      proxy.registerHandler((e: EventContainer) => {}, eventType)
      awaitCond(received.size == 1)
    }
    "when publishing an event, sends the event to the server" in {
      val clientId = "clientId"
      val proxy = new TardisProxy(clientId, busStub, system.actorOf(TardisProxyActor.props(busStub)))
      val eventType = "foo"
      val event = EventContainer(UUID.randomUUID, eventType, "payload", clientId)
      val assertAck = { ack: Ack => assert(ack.id === event.id) }
      proxy.publish(event, assertAck)
      awaitCond(received.contains(event))
    }
    // "when receiving an ack from the server, sends it to the confirmation function" in {
    //   val clientId = "clientId"
    //   val proxy = new TardisProxy(clientId, busStub, system.actorOf(TardisProxyActor.props(busStub)))
    //   val eventType = "foo"
    //   val event = EventContainer(UUID.randomUUID, eventType, "payload", clientId)
    //   var receivedAck: Option[Ack] = None
    //   val assertAck = { ack: Ack => receivedAck = Some(ack) }
    //   proxy.publish(event, assertAck)
    //   awaitCond(receivedAck.isDefined)
    //   assert(receivedAck.get.id === event.id)
    // }
    // "when receiving an event from the server, sends it to the registered handler" in {
    // }
    // "when given an ack, sends it to the server" in {
    // }
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
      sender ! Ack(evt.id)
    }
    case x: Any => {
      println(s"Test actor received $x")
      received.append(x)
    }
  }
}
