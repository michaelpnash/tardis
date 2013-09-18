package client

import akka.actor._
import com.jglobal.tardis._
import org.scalatest.FreeSpec
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import java.util.UUID
import scala.concurrent.duration._

/* Port 8888 */
class SpecMultiJvmNode1 extends FreeSpec with BeforeAndAfterAll {
  "the tardis client" - {
    "should send an event and receive the appropriate ack" in {
      println("Start test 1")
      val proxy = TardisProxy("client1", "127.0.0.1:9999")
      println("Proxy instantiated! *********************")
      var ackReceived: Option[Ack] = None
      val receiver = {
        ack: Ack => ackReceived = Some(ack)
        println(s"In node1 test, got back the ack, it was $ack")
      }
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientid")
      proxy.publish(event, receiver)
      Thread.sleep(5000)
      assert(ackReceived.get === Ack(event.id))
      println(s"Got the ack ${ackReceived.get}")
    }
  }
}

/* Port 9999 */
class SpecMultiJvmNode2 extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("tardis")
  "the tardis server" - {
    "should start up and run while the other tests complete" in {
      println("Start test 2")
      val module = new infrastructure.TardisModule(system)
      val router = module.eventRouterActor
      println(s"------------------------ Event Router actor at ${router.path}")
      router ! "WakeyWakey"
      Thread.sleep(8000)
    }
  }

  override def afterAll() { system.shutdown  }
}

/* Port 0, e.g. find a random free port */
class SpecMultiJvmNode3(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender with BeforeAndAfterAll {
  def this() = this(ActorSystem("subscriber"))

  println("Start test 3")

  // "the tardis client" - {
  //   "should receive an event published by another client" in {
  //   }
  // }
  val bus = system.actorSelection("akka.tcp://tardis@127.0.0.1:9999/user/eventrouter")
  "the server should respond with an OK message when sent a subscription" in {
    bus ! Subscription("sub1", List("test"))
    expectMsg("Ok")
    println("GOT THE OK! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&")
  }
  "the server should reply with an identity too" in {
    bus ! Identify
    val identityReply = expectMsgClass(5 seconds, classOf[ActorIdentity])
    println(s"Got reply $identityReply")
  }
  // "the server should send back an event I'm subscribed to" in {
  //   //bus ! Subscription("sub2", List("test"))
  //   val event = EventContainer(UUID.randomUUID, "test", "payload", "sub2")
  //   //bus ! event
  //   //expectMsg("Ok")
  //   //expectMsg(Ack(event.id))
  //   //expectMsg(event)
  // }
  
  override def afterAll() { system.shutdown }
}
