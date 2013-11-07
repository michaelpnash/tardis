package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._
import infrastructure.TestKitUsageSpec

class ClientRepositoryTest extends FreeSpec with BeforeAndAfterAll {
  implicit val system = ActorSystem("test-client-repo", ConfigFactory.parseString(TestKitUsageSpec.config))

  val ref1 = system.actorOf(Props[TestActor])
  val ref2 = system.actorOf(Props[TestActor])
  
  "a client repository" - {
    "will create a new client with the specified id when finding a client that does not exist" in {
      val repo = new TransientClientRepository
      val id = "foo"
      val client = repo.findOrCreate(id)
      assert(client.id === id)
    }
    "will return an existing client when it does exist" in {
      val repo = new TransientClientRepository
      val client = Client("bar", subscribes = Set(EventType("name")))
      repo.store(client)
      assert(repo.findOrCreate("bar") === client)
    }
    "will overwrite a client with the same id when storing" in {
      val repo = new TransientClientRepository
      val client = Client("baz", subscribes = Set(EventType("name")))
      repo.store(client)
      val newBaz = Client("baz", subscribes = Set(EventType("other")))
      repo.store(newBaz)
      assert(repo.findOrCreate("baz") === newBaz)
    }
    "will update client with new node and subscription information" in {
      val repo = new TransientClientRepository
      val id = "bar"
      val fooType = "foo"
      repo.recordSubscription(ref1, Subscription(id, List(fooType)))
      val updatedClient = repo.findOrCreate(id)
      assert(updatedClient.nodes.size === 1)
      val firstNode = updatedClient.nodes.head
      assert(firstNode.lastSubscription > 0)
      assert(firstNode.ref === ref1)
      assert(updatedClient.subscribes === Set(EventType(fooType)))
    }
    "will update a client with new publishes information" in {
      val repo = new TransientClientRepository
      val id = "bar"
      val client = Client(id)
      repo.store(client)
      val publishedType = "other"
      repo.recordPublished(id, publishedType)
      assert(repo.findOrCreate(id).publishes === Set(EventType(publishedType)))
    }
    "can return a set of all clients that subscribe to a specified event type" in {
      val type1 = "type1"
      val type2 = "type2"
      val repo = new TransientClientRepository
      val client1 = repo.store(Client("id1", subscribes = Set(EventType(type1), EventType(type2))))
      val client2 = repo.store(Client("id2", subscribes = Set(EventType(type1))))
      repo.store(Client("id3", subscribes = Set(EventType(type2))))
      assert(repo.subscribersOf(EventType(type1)) === List(client1, client2))
    }
    "can produce a stats object for any client that has stats recorded for it" in {
      val repo = new TransientClientRepository
      val clientId = "clientId"
      repo.recordAck(clientId)
      assert(repo.stats(clientId).clientId === clientId)
    }
    "will increment the events sent count and last timestamp when requested" in {
      val repo = new TransientClientRepository
      val clientId = "id1"
      repo.recordEventSent(clientId)
      assert(repo.stats(clientId).eventsSentTo.count === 1)
      assert(repo.stats(clientId).eventsSentTo.last > 0)      
    }
    "will increment the acks received from count and last timestamp when requested" in {
      val repo = new TransientClientRepository
      val clientId = "id2"
      repo.recordAck(clientId)
      assert(repo.stats(clientId).acks.count === 1)
      assert(repo.stats(clientId).acks.last > 0)

    }
    "will increment the events received from count and last timestamp when requested" in {
      val repo = new TransientClientRepository
      val clientId = "id3"
      repo.recordEventReceived(clientId)
      assert(repo.stats(clientId).eventsReceivedFrom.count === 1)
      assert(repo.stats(clientId).eventsReceivedFrom.last > 0)
    }
    "will reset the counts when requested" in {
      val repo = new TransientClientRepository
      val clientId = "id4"
      repo.recordEventSent(clientId)
      assert(repo.stats(clientId).eventsSentTo.count === 1)
      assert(repo.stats(clientId).eventsSentTo.last > 0)
    }
  }

 override def afterAll() {
   system.shutdown()
 }
}
