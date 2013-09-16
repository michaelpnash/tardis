package client
import akka.actor.ActorSystem

import org.scalatest.FreeSpec
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import akka.testkit.ImplicitSender

class SpecMultiJvmNode1(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender {
  def this() = this(ActorSystem("client"))
  
  "a thing" - {
    "should send an event and receive an ack" in {
      val bus = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
      bus ! "test"
      expectMsg("ack")
    }
  }
}

class SpecMultiJvmNode2 extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("tardis")
  "a thing" - {
    "should do a thing" in {
      val module = new infrastructure.TardisModule(system)
      Thread.sleep(5000)
      //running(FakeApplication()) {
      //}
    }
  }

  override def afterAll() {
    system.shutdown
  }
}
