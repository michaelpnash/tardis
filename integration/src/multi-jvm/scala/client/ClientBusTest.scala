package client

import akka.actor._
import com.jglobal.tardis._
import org.scalatest.FreeSpec
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import java.util.UUID
import scala.concurrent.duration._

trait AwaitAssert {
  def awaitAssert(f:() => Boolean, interval: Long = 20L, maxTime: Long = 30000, msg: String = "Assertion timed out") {
    var time = 0L
    while (f() == false) {
      time += interval
      if (time > maxTime) throw new RuntimeException(msg)
      Thread.sleep(interval)
    }
  }
}

/* Port 8888 */
class SpecMultiJvmNode1 extends FreeSpec with BeforeAndAfterAll with AwaitAssert {
  val clientId = "client1"
  var proxy: TardisProxy = _
  "the tardis client" - {
    "should send an event and receive the appropriate ack" in {
      proxy = new TardisProxy(clientId)
      awaitAssert(() => proxy.ping)
      var ackReceived: Option[Ack] = None
      val receiver = { ack: Ack => ackReceived = Some(ack) }
      val event = EventContainer(UUID.randomUUID, "type1", "payload", clientId)
      proxy.publish(event, receiver)
      awaitAssert(() => ackReceived.isDefined, msg = "Never got an ack from publish")
      assert(ackReceived.get === Ack(event.id, event.clientId))
    }
  }
  override def afterAll() {
    if (proxy != null) proxy.shutdown
  }
}

//Port 9999
class SpecMultiJvmNode2 extends FreeSpec with BeforeAndAfterAll with AwaitAssert {
  val system = ActorSystem("application")
  "the tardis server" - {
    "should start up and run while the other tests complete" in {
      println("Server starts up")
      val module = infrastructure.TardisModule.start(system)
      Thread.sleep(75000) //TODO: Have a message that shuts down the actor
      println("Server shuts down")
    }
  }

  override def afterAll() { if (system != null) system.shutdown  }
}

/* Port 0, e.g. find a random free port */
class SpecMultiJvmNode3 extends FreeSpec with BeforeAndAfterAll with AwaitAssert {
  val clientId = "clientNode3"
  var proxy: TardisProxy = _
  "the tardis proxy" - {
    "when sent an event that I subscribe to, should return that event to the proper handler" in {
      proxy = new TardisProxy(clientId)
      awaitAssert(() => proxy.ping)
      var evtReceived: Option[EventContainer] = None
      val handler = { evt: EventContainer => evtReceived = Some(evt) }
      val event = EventContainer(UUID.randomUUID, "type2", "payload", clientId)
      proxy.registerHandler(handler, "type2")
      Thread.sleep(2000)
      proxy.publish(event, { ack: Ack => {} }) // discard the ack WE get back
      awaitAssert(() => evtReceived.isDefined) // wait to receive the event
      assert(evtReceived.get === event) // make sure it's the one we sent
      proxy.ack(evtReceived.get.id)
      awaitAssert(() => proxy.stats(clientId).acks.count > 0) // wait for the bus to say we've acked receipt
    }
  }
  override def afterAll() { if (proxy != null) proxy.shutdown }
}
