package controllers

import domain.{ClientRepository, Client}
import com.jglobal.tardis._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import infrastructure.SerializableClient._

class Application(clientRepo: ClientRepository) extends Controller {

  case class ClientInfo(client: ClientDAO, stats: ClientStats)

  implicit val countAndLastWrites: Writes[CountAndLast] = (
    (JsPath \ "count").write[Long] and
    (JsPath \ "last").write[Long]
  )(unlift(CountAndLast.unapply))
  
  implicit val clientStatsWrites: Writes[ClientStats] = (
    (JsPath \ "clientId").write[String] and
    (JsPath \ "eventsSentTo").write[CountAndLast] and
    (JsPath \ "acks").write[CountAndLast] and
    (JsPath \ "eventsReceivedFrom").write[CountAndLast]
  )(unlift(ClientStats.unapply))
  
  implicit val clientInfoWrites: Writes[ClientInfo] = (
    (JsPath \ "client").write[ClientDAO] and
    (JsPath \ "stats").write[ClientStats]
  )(unlift(ClientInfo.unapply))
  
  def index = Action {
    implicit request =>
    Ok(Json.toJson(clientRepo.list.map(client => ClientInfo(ClientDAO(client.id, client.subscribes.map(_.name), client.publishes.map(_.name)), clientRepo.stats(client.id)))))
  }
}
