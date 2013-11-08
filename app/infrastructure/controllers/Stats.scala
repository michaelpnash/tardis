package infrastructure.controllers

import domain.ClientRepository
import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator}
import play.api.libs.iteratee.Concurrent._
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._

class Stats(statsEnumerator: Enumerator[JsValue], statsChannel: Channel[JsValue], clientRepo: ClientRepository) extends Controller {
  require(statsEnumerator != null)
  require(statsChannel != null)

  def index = Action { Ok(views.html.index("TARDIS Status")) }

  /** Enumeratee for filtering messages based on room */
  def filter(room: String) = Enumeratee.filter[JsValue] { json: JsValue => (json \ "room").as[String] == room }

  /** Enumeratee for detecting disconnect of SSE stream */
  def connDeathWatch(addr: String): Enumeratee[JsValue, JsValue] =
    Enumeratee.onIterateeDone{ () => println(addr + " - SSE disconnected") }

  /** Controller action serving activity based on room */
  def chatFeed(room: String) = Action { req =>
    println(req.remoteAddress + " - SSE connected")

    play.libs.Akka.system.scheduler.scheduleOnce(2 seconds) {
      val statsActor = play.libs.Akka.system.actorSelection("/user/ChatterSupervisor/doctor")
      clientRepo.list.foreach(client => statsActor ! clientRepo.stats(client.id))
      println(s"Pushing initial stats for ${clientRepo.list.size} clients")
    }
    Ok.chunked(statsEnumerator
      &> filter(room)
      &> Concurrent.buffer(50)
      &> connDeathWatch(req.remoteAddress)
      &> EventSource()
    ).as("text/event-stream")
  }
}
