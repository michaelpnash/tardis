package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._

class ClientRepositoryTest extends FreeSpec with BeforeAndAfterAll {
  val config = ConfigFactory.load()
  val system = ActorSystem("test-client-repo", config.getConfig("test").withFallback(config))
  
  val ref1 = system.actorOf(Props[TestActor1])
  val ref2 = system.actorOf(Props[TestActor2])
  
  "a client repository" - {
    "will create a new client with the specified id when finding a client that does not exist" in {
      val repo = new ClientRepository
      val id = "foo"
      val client = repo.findOrCreate(id)
      assert(client.id === id)
    }
    "will return an existing client when it does exist" in {
      val repo = new ClientRepository
      val client = Client("bar", subscribes = Set(EventType("name", "descrip")))
      repo.store(client)
      assert(repo.findOrCreate("bar") === client)
    }
    "will overwrite a client with the same id when storing" in {
      val repo = new ClientRepository
      val client = Client("baz", subscribes = Set(EventType("name", "descrip")))
      repo.store(client)
      val newBaz = Client("baz", subscribes = Set(EventType("other", "some")))
      repo.store(newBaz)
      assert(repo.findOrCreate("baz") === newBaz)
    }
    "will update client with new node and subscription information" in {
      val repo = new ClientRepository
      val id = "bar"
      val client = Client(id)
      val fooType = "foo"
      repo.recordSubscription(ref1, Subscription(id, List(fooType)))
      val updatedClient = repo.findOrCreate(id)
      assert(updatedClient.nodes.size === 1)
      val firstNode = updatedClient.nodes.head
      assert(firstNode.lastSubscription > 0)
      assert(firstNode.ref === ref1)
      assert(updatedClient.subscribes === Set(EventType(fooType, "")))
    }
    "will update a client with new publishes information" in {
      val repo = new ClientRepository
      val id = "bar"
      val client = Client(id)
      val fooType = "foo"
      repo.store(client)
      val publishedType = "other"
      repo.recordPublished(id, publishedType)
      assert(repo.findOrCreate(id).publishes === Set(EventType(publishedType, "")))
    }
    "can return a set of all clients that subscribe to a specified event type" in {
      val type1 = "type1"
      val type2 = "type2"
      val repo = new ClientRepository
      val client1 = repo.store(Client("id1", subscribes = Set(EventType(type1), EventType(type2))))
      val client2 = repo.store(Client("id2", subscribes = Set(EventType(type1))))
      val client3 = repo.store(Client("id3", subscribes = Set(EventType(type2))))
      assert(repo.subscribersOf(EventType(type1)) === List(client1, client2))
    }
  }

 override def afterAll() {
   system.shutdown
 }
}
