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
      bus ! EventContainer(id, "type", "payload", "clientid")
      expectMsg(Ack(id))
    }
  }
}

class SpecMultiJvmNode2 extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("tardis")
  "the event-routing server" - {
    "should start up" in {
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

class SpecMultiJvmNode3(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender with BeforeAndAfterAll {
  def this() = this(ActorSystem("subscriber"))

  val bus = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
  "the server should respond with an OK message when sent a subscription" in {
    bus ! Subscription("sub1", List("test"))
    expectMsg("Ok")
  }
  "the server should send back an event I'm subscribed to" in {
    //bus ! Subscription("sub2", List("test"))
    val event = EventContainer(UUID.randomUUID, "test", "payload", "sub2")
    //bus ! event
    //expectMsg("Ok")
    //expectMsg(Ack(event.id))
    //expectMsg(event)
  }
  
  override def afterAll() {
    system.shutdown
  }
}
