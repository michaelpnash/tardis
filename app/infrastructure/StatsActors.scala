package akka

import akka.actor._
import com.jglobal.tardis.{ClientStats, CountAndLast}
import domain.Client
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.functional.syntax._
import infrastructure.SerializableClient._
import scala.language.postfixOps
import scala.concurrent.duration._
import org.joda.time.DateTime
import scala.util.Random

import infrastructure.SerializableClient._
import play.api.libs.iteratee.Concurrent.Channel
import infrastructure.controllers.ChatApplication

object ClientInfo {
  
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
  
  def toJson(client: ClientDAO, stats: ClientStats) = Json.toJson(ClientInfo(client, stats))
  
}

object StatsActors {

  def start(system: ActorSystem, chatChannel: Channel[JsValue]) {
    val supervisor = system.actorOf(Props(new Supervisor(chatChannel)), "ChatterSupervisor")
  }
}

/** Supervisor initiating Romeo and Juliet actors and scheduling their talking */
class Supervisor(chatChannel: Channel[JsValue]) extends Actor {
  val statsSender = context.actorOf(Props(new Chatter("Doctor", Seq(), chatChannel)), "doctor")
  def receive = { case _ => }
}

/** Chat participant actors picking quotes at random when told to talk */
class Chatter(name: String, quotes: Seq[String], chatChannel: Channel[JsValue]) extends Actor {
  
  def receive = {
    case client: Client => {
      val now: String = DateTime.now.toString
      val msg = Json.obj("room" -> "room1", "text" -> client.toString, "user" -> "doctor", "time" -> now)
      chatChannel.push(msg)
    }
    case clientStats: ClientStats => {
      val info = ClientInfo.toJson(ClientDAO(clientStats.clientId, Set(), Set()), clientStats)
      val now: String = DateTime.now.toString
      
      val msg = Json.obj("id" -> clientStats.clientId, "room" -> "room1", "text" -> clientStats.toString, "user" -> "doctor", "time" -> now, "publishes" -> "", "sentTo" -> clientStats.eventsSentTo.count, 
          "receivedFrom" -> clientStats.eventsReceivedFrom.count, "acksFrom" -> clientStats.acks.count)
      chatChannel.push(msg)
    }
  }
}
