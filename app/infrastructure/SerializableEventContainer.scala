package infrastructure

import akka.actor.ActorSystem
import domain.EventType
import domain.Client
import com.jglobal.tardis.EventContainer

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.UUID

object SerializableEventContainer {
  implicit object UUidWrites extends Writes[UUID] {
    def writes(u: UUID) = JsString(u.toString)
  }

  implicit object UuidReads extends Reads[UUID] {
    def reads(json: JsValue): JsResult[UUID] = json match {
      case JsString(x) => JsSuccess(UUID.fromString(x))
      case _ => JsError("Expected UUID as JsString")
    }
  }
  
    implicit val containerWrites: Writes[EventContainer] = (
      (JsPath \ "id").write[UUID] and
      (JsPath \ "eventType").write[String] and
      (JsPath \ "payload").write[String] and
      (JsPath \ "clientId").write[String]
  )(unlift(EventContainer.unapply))

  implicit val containerReads: Reads[EventContainer] = (
    (JsPath \ "id").read[UUID] and
      (JsPath \ "eventType").read[String] and
      (JsPath \ "payload").read[String] and
      (JsPath \ "clientId").read[String]      
  )(EventContainer.apply _)

  def toStr(original: EventContainer) = Json.toJson(original).toString
  def fromStr(str: String) = Json.parse(str).as[EventContainer]
}



