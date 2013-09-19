package infrastructure

import akka.actor.ActorSystem
import domain.EventType
import org.scalatest.FreeSpec
import domain.Client

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

class PersistentClientRepositoryTest extends FreeSpec {
  "the persistent client repository" - {
    "can persist clients between instantiations" in {
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




