package infrastructure

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

object EventContainer {
  implicit object UUIDFormat extends Format[UUID] {
    def writes(uuid: UUID): JsValue = JsString(uuid.toString())
    def reads(json: JsValue): JsResult[UUID] = json match {
      case JsString(x) => JsSuccess(UUID.fromString(x))
      case _ => JsError("Expected UUID as JsString")
    }
  }
  
  implicit val containerReads: Reads[EventContainer] = (
    (JsPath \ "id").read[UUID] and
      (JsPath \ "type").read[String] and
      (JsPath \ "payload").read[String]
  )(EventContainer.apply _)

  implicit val containerWrites: Writes[EventContainer] = (
    (JsPath \ "id").write[UUID] and
      (JsPath \ "type").write[String] and
      (JsPath \ "payload").write[String]
  )(unlift(EventContainer.unapply))

  val idTag = "id"
  val typeTag = "type"
  val payloadTag = "payload"
  def fromJSON(str: String): EventContainer = Json.parse(str).as[EventContainer]
  def toJSON(eventContainer: EventContainer): String = Json.stringify(Json.toJson(eventContainer))
}

case class EventContainer(id: UUID, eventType: String, payload: String)
