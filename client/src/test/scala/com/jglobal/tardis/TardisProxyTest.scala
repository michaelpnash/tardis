package com.jglobal.tardis

import org.scalatest.FreeSpec
import akka.actor._
import com.typesafe.config._
import akka.testkit._

class TardisProxyTest(system: ActorSystem) extends TestKit(system) with FreeSpec {
  def this() = this(ActorSystem("test-proxy", ConfigFactory.load("client").getConfig("test")))

  implicit val implSys = system
  
  val busStub = TestActorRef(Props[TestActor])
  
  "the tardis proxy" - {
    "when a handler is registered, add the type to the list of subscribed types sent to the server" in {
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

trait Recording {
  val received = collection.mutable.ListBuffer[EventContainer]()
}

class TestActor extends Actor with Recording {
  def receive = {
    case container: EventContainer => {
      received.append(container)
      assert(received.size > 0)
    }
    case _ => throw new IllegalArgumentException("Unknown message")
  }
}
