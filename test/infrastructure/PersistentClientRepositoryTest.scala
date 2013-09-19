package infrastructure

import akka.actor.ActorSystem
import domain.EventType
import org.scalatest.FreeSpec
import domain.Client

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

class PersistentRepositoryTest extends FreeSpec {
  "the persistent client repository" - {
    "can persist clients between instantiations" in {
    }
  }
}

object SerializableClient {
  case class ClientDAO(id: String, subscribes: Set[String], publishes: Set[String])
  implicit val clientDAOWrites: Writes[ClientDAO] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "subscribes").write[Set[String]] and
      (JsPath \ "publishes").write[Set[String]]
  )(unlift(ClientDAO.unapply))

  implicit val clientDAOReads: Reads[ClientDAO] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "subscribes").read[Set[String]] and
      (JsPath \ "publishes").read[Set[String]]
  )(ClientDAO.apply _)

  def toStr(original: Client) = Json.toJson(ClientDAO(original.id, original.subscribes.map(_.name), original.publishes.map(_.name))).toString
  def fromStr(str: String)(implicit system: ActorSystem) = {
    val dao = Json.parse(str).as[ClientDAO]
    Client(dao.id, subscribes = dao.subscribes.map(t => EventType(t)), publishes = dao.publishes.map(t => EventType(t)))
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




