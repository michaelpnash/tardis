package infrastructure

import akka.actor._
import com.jglobal.tardis.ClientStats
import domain.{ClientRepository, Client}
import play.api.libs.json._
import play.api.libs.json.Json

import scala.language.postfixOps
import org.joda.time.DateTime

import play.api.libs.iteratee.Concurrent.Channel

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
