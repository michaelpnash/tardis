package infrastructure

import akka.actor.ActorSystem
import domain.EventType
import org.scalatest.FreeSpec
import domain.Client

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.io.File

class PersistentClientRepositoryTest extends FreeSpec {
  val testDir = "/tmp"
  "the persistent client repository" - {
    "can persist clients between instantiations" in {
      val target = new File(testDir + "/test/")
      target.mkdirs()
      target.delete()
      val repo = new PersistentClientRepository(target.getPath, null)
      val client1 = Client("id1", subscribes = Set(EventType("one"), EventType("two")), publishes = Set(EventType("two"), EventType("three")))(null)
      val client2 = Client("id2", subscribes = Set(EventType("three"), EventType("two")), publishes = Set(EventType("four"), EventType("three")))(null)
      repo.store(client1)
      repo.store(client2)

      val repo2 = new PersistentClientRepository(target.getPath, null)
      assert(repo2.findOrCreate(client1.id)(null) === client1)
      assert(repo2.findOrCreate(client2.id)(null) === client2)
    }
  }
}

class SerializableClientTest extends FreeSpec {
  "the serializable client" - {
    "can be serialized and deserialized to a string" in {
      val client = Client("someid", subscribes = Set(EventType("foo"), EventType("bar")), publishes = Set(EventType("baz")))(null)
      val str = SerializableClient.toStr(client)
      val result = SerializableClient.fromStr(str)(null)
      assert(result === client)
    }
  }
}




