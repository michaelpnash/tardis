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
      val proxy = TardisProxy("client1", "127.0.0.1:9999")
      var ackReceived: Option[Ack] = None
      val receiver = { ack: Ack => ackReceived = Some(ack) }
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientid")
      proxy.publish(event, receiver)
      Thread.sleep(5000)
      assert(ackReceived.get === Ack(event.id))
    }
  }
}

/* Port 9999 */
class SpecMultiJvmNode2 extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("tardis")
  "the tardis server" - {
    "should start up and run while the other tests complete" in {
      val module = new infrastructure.TardisModule(system)
      Thread.sleep(8000)
    }
  }

  override def afterAll() { system.shutdown  }
}

/* Port 0, e.g. find a random free port */
class SpecMultiJvmNode3 extends FreeSpec with BeforeAndAfterAll {
  "the tardis proxy" - {
    "when sent an event that I subscribe to, should return that event to the proper handler" in {
      val proxy = TardisProxy("client1", "127.0.0.1:9999")
      var evtReceived: Option[EventContainer] = None
      val handler = { evt: EventContainer => evtReceived = Some(evt) }
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientid")
      proxy.registerHandler(handler, "type")
      Thread.sleep(2000)
      proxy.publish(event, { ack: Ack => {} })
      Thread.sleep(5000)
      assert(evtReceived.get === event)
    }
  }
}
