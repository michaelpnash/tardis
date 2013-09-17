package domain

import org.scalatest.{FreeSpec, BeforeAndAfterAll}
import akka.actor._
import com.jglobal.tardis._

class ClientRepositoryTest extends FreeSpec with BeforeAndAfterAll {
  val system = ActorSystem("test-client-repo")

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
      val client = Client("bar", subscribes = List(EventType("name", "descrip")))
      repo.store(client)
      assert(repo.findOrCreate("bar") === client)
    }
    "will overwrite a client with the same id when storing" in {
      val repo = new ClientRepository
      val client = Client("baz", subscribes = List(EventType("name", "descrip")))
      repo.store(client)
      val newBaz = Client("baz", subscribes = List(EventType("other", "some")))
      repo.store(newBaz)
      assert(repo.findOrCreate("baz") === newBaz)
    }
    "will update client with new node and subscription information" in {
      val repo = new ClientRepository
      val id = "bar"
      val client = Client(id)
      val fooType = "foo"
      repo.store(client)
      repo.recordSubscription(ref1, Subscription(id, List(fooType)))

    }
    "will update a client with new publishes information" in {
    }
  }

 override def afterAll() {
   system.shutdown
 }
}
