package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._

class ClientTest extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("test")
  
  val ref1 = system.actorOf(Props[TestActor1])
  def ref2 = system.actorOf(Props[TestActor2])
  
  "the client" - {
    "can add a new node to itself, returning the new client" in {
      val client = Client("foo")
      val node = ClientNode(ref1, 0l)
      val withNode = client.withNode(node)
      assert(withNode.nodes === Set(node))
    }
    "will not add the same node again" in {
      val client = Client("foo")
      val node = ClientNode(ref1, 0l)
      val node2 = ClientNode(ref1, 0l)
      val withNode = client.withNode(node).withNode(node2)
      assert(withNode.nodes === Set(node))
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
    "can update a node's last subscription date, returning a new client" in {
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
