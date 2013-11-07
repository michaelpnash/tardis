package akka

import akka.actor._
import com.jglobal.tardis.{ClientStats, CountAndLast}
import domain.{ClientRepository, Client}
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
import infrastructure.controllers.Stats

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

  def start(system: ActorSystem, statsChannel: Channel[JsValue], clientRepo: ClientRepository) {
    val supervisor = system.actorOf(Props(new Supervisor(statsChannel, clientRepo)), "ChatterSupervisor")
  }
}

class Supervisor(statsChannel: Channel[JsValue], clientRepo: ClientRepository) extends Actor {
  val statsSender = context.actorOf(Props(new StatsActor("Doctor", clientRepo, statsChannel)), "doctor")
  def receive = { case _ => }
}

class StatsActor(name: String, clientRepo: ClientRepository, statsChannel: Channel[JsValue]) extends Actor {
  
  def receive = {
    case client: Client => {
      val now: String = DateTime.now.toString
      val msg = Json.obj("room" -> "room1", "text" -> client.toString, "user" -> "doctor", "time" -> now)
      statsChannel.push(msg)
    }
    case clientStats: ClientStats => {
      val info = ClientInfo.toJson(ClientDAO(clientStats.clientId, Set(), Set()), clientStats)
      val now: String = DateTime.now.toString
      val client = clientRepo.findOrCreate(clientStats.clientId)
      val msg = Json.obj("id" -> clientStats.clientId,
        "publishes" -> client.publishes.map(_.name).mkString(","),
        "subscribes" -> client.subscribes.map(_.name).mkString(","),
        "nodes" -> client.nodes.size,
        "room" -> "room1", "text" -> clientStats.toString, "user" -> "doctor", "time" -> now, "publishes" -> "", "sentTo" -> clientStats.eventsSentTo.count,
          "receivedFrom" -> clientStats.eventsReceivedFrom.count, "acksFrom" -> clientStats.acks.count)
      statsChannel.push(msg)
    }
  }
}
