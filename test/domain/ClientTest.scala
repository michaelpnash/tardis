package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._
import com.typesafe.config._
import com.jglobal.tardis._
import akka.testkit._
import java.util.UUID

class ClientTest(system: ActorSystem) extends TestKit(system) with FreeSpec with BeforeAndAfterAll {
  def this() = this(ActorSystem("test-router", ConfigFactory.load().getConfig("test")))

  implicit val implSys = system

  val ref1 = TestActorRef(Props[TestActor])
  val actor1: Recording = ref1.underlyingActor
  val ref2 = TestActorRef(Props[TestActor])
  val actor2: Recording = ref2.underlyingActor
  
  "the client" - {
    "can add a new node to itself, returning the new client" in {
      val client = Client("foo")
      val node = ClientNode(ref1, 0l)
      val withNode = client.withNode(node)
      assert(withNode.nodes === Set(node))
    }
    "will update and not duplicate an existing node" in {
      val client = Client("foo")
      val node = ClientNode(ref1, 0l)
      val node2 = ClientNode(ref1, 14l)
      val withNode = client.withNode(node).withNode(node2)
      assert(withNode.nodes === Set(node2))
    }
    "can remove nodes that have not been heard from in a set timeout" in {
      val client = Client("foo")
      val node = ClientNode(null, 0l)
      val node2 = ClientNode(null, System.currentTimeMillis)
      val withAll = client.withNode(node).withNode(node2)
      val without = withAll.withoutStaleNodes
      assert(without.nodes === Set(node2))
    }
    "can route messages to one of it's nodes" in {
      val type1 = "type1"
      val client = Client("id", Set(ClientNode(ref1, 0l), ClientNode(ref2, 0l)), subscribes = Set(EventType(type1)))
      val event = EventContainer(UUID.randomUUID, type1, "payload", client.id)
      client.sendEvent(event)
      awaitAssert(actor1.received.toList ++ actor2.received.toList === List(event))
    }
    "can add to it's list of subscribed types" in {
      val type1 = "type1"
      val type2 = "type2"
      val client = Client("id")
      val updated = client.withSubscriptions(List(type1, type2))
      assert(updated.subscribes === Set(EventType(type1, ""), EventType(type2, "")))
    }
    "can add to it's list of published types" in {
      val type1 = "type1"
      val type2 = "type2"
      val client = Client("id", publishes = Set(EventType(type1, "")))
      val updated = client.withPublishes(type2)
      assert(updated.publishes === Set(EventType(type1, ""), EventType(type2, "")))
    }
  }

  override def afterAll() {
    system.shutdown
  }
}

trait Recording {
  val received = collection.mutable.ListBuffer[EventContainer]()
}

class TestActor extends Actor with Recording {
  def receive = {
    case container: EventContainer => {
      println(s"Test actor received $container")
      received.append(container)
      assert(received.size > 0)
    }
    case _ => throw new IllegalArgumentException("Unknown message")
  }
}
