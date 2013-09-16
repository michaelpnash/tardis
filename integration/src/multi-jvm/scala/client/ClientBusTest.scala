package client
import akka.actor.ActorSystem

import com.jglobal.tardis._
import org.scalatest.FreeSpec
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import java.util.UUID

class SpecMultiJvmNode1(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender {
  def this() = this(ActorSystem("client"))
  
  "a thing" - {
    "should send an event and receive the appropriate ack" in {
      val bus = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
      val id = UUID.randomUUID
      bus ! EventContainer(id, "type", "payload")
      expectMsg(Ack(id))
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
