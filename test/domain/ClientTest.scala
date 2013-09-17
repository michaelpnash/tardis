package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._

class ClientTest extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("test-client")
  
  val ref1 = system.actorOf(Props[TestActor1])
  val ref2 = system.actorOf(Props[TestActor2])
  
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
      val client = Client("id", publishes = Set(EventType(type2, "")))
      val updated = client.withPublishes(type2)
      assert(updated.subscribes === Set(EventType(type1, ""), EventType(type2, "")))
    }
  }

  override def afterAll() {
    system.shutdown
  }
}

class TestActor1 extends Actor {
  def receive = {
    case _ => {}
  }
}

class TestActor2 extends Actor {
  def receive = {
    case _ => {}
  }
}
