package infrastructure

import java.util.UUID
import play.api.libs.json._

object Ack {
  val idTag = "id"
  def fromJSON(str: String): Ack = {
    (Json.parse(str) \ idTag).validate[String] match {
      case JsSuccess(value, path) => Ack(UUID.fromString(value))
      case JsError(errors) => throw new IllegalArgumentException(s"Can't parse Ack from json:$str, ${errors.mkString}")
    }
  }
  def toJSON(ack: Ack): String = Json.stringify(Json.obj(idTag -> ack.id.toString))
}

case class Ack(id: UUID)
