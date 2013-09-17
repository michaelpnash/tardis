package com.jglobal.tardis

import org.scalatest.FreeSpec
import akka.actor._
import com.typesafe.config._
import akka.testkit._
import collection.mutable.ListBuffer

class TardisProxyTest(system: ActorSystem) extends TestKit(system) with FreeSpec {
  def this() = this(ActorSystem("test-proxy", ConfigFactory.load("client").getConfig("test")))

  implicit val implSys = system
  val received = collection.mutable.ListBuffer[Any]()

  val busRef = system.actorOf(TestActor.props(received))
  val busStub = ActorSelection(busRef, busRef.path.toString)
  
  "the tardis proxy" - {
    "when a handler is registered, add the type to the list of subscribed types sent to the server" in {
      val proxy = new TardisProxy("clientId", busStub, system.actorOf(TardisProxyActor.props(busStub)))
      val eventType = "foo"
      proxy.registerHandler((e: EventContainer) => {}, eventType)
      awaitAssert(received.size === 1)
    }
    "when publishing an event, sends the event to the server" in {
    }
    "when receiving an ack from the server, sends it to the confirmation function" in {
    }
    "when receiving an event from the server, sends it to the registered handler" in {
    }
    "when given an ack, sends it to the server" in {
    }
  }
}

object TestActor {
  def props(received: ListBuffer[Any]) = Props(classOf[TestActor], received)
}

class TestActor(received: ListBuffer[Any]) extends Actor {
  def receive = {
    case x: Any => received.append(x)
  }
}
