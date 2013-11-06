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

object ChatActors {

  def start(system: ActorSystem, chatChannel: Channel[JsValue]) {

    /** Supervisor for Romeo and Juliet */
    val supervisor = system.actorOf(Props(new Supervisor(chatChannel)), "ChatterSupervisor")
  }

  case object Talk
}

/** Supervisor initiating Romeo and Juliet actors and scheduling their talking */
class Supervisor(chatChannel: Channel[JsValue]) extends Actor {
  val doctor = context.actorOf(Props(new Chatter("Doctor", Seq(), chatChannel)), "doctor")
  println("Doctor at " + doctor.path)
  def receive = { case _ => }
}

/** Chat participant actors picking quotes at random when told to talk */
class Chatter(name: String, quotes: Seq[String], chatChannel: Channel[JsValue]) extends Actor {
  
  def receive = {
//    case str: String => {
//      println(s"Got a string in chatter $str")
//      val now: String = DateTime.now.toString
//      val msg = Json.obj("room" -> "room1", "text" -> str, "user" -> name, "time" -> now)
//      ChatApplication.chatChannel.push(msg)
//    }
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
      //println("Msg is a " + msg.getClass.getName)
      println("Info is a " + info.getClass.getName)
      //val msg = info
      println(s"Pushing status $msg to client")
      println(s"Not sending $info to client")
      chatChannel.push(msg)
    }
    case ChatActors.Talk  => {
      val now: String = DateTime.now.toString
      val quote = quotes(Random.nextInt(quotes.size))
      val msg = Json.obj("room" -> "room1", "text" -> quote, "user" ->  name, "time" -> now )

      chatChannel.push(msg)
    }
  }
}

object Quotes {
  val juliet = Seq("O Romeo, Romeo! wherefore art thou Romeo? \nDeny thy father and refuse thy name; \nOr, if thou wilt not, be but sworn my love, \nAnd I'll no longer be a Capulet. ", "By whose direction found'st thou out this place?", "I would not for the world they saw thee here.", "What man art thou that, thus bescreened in night, \nSo stumblest on my counsel?", "If they do see thee, they will murder thee.", "My ears have yet not drunk a hundred words \n Of thy tongue's uttering, yet I know the sound.\nArt thou not Romeo, and a Montague?", "How cam'st thou hither, tell me, and wherefore?\nThe orchard walls are high and hard to climb,\nAnd the place death, considering who thou art,\nIf any of my kinsmen find thee here.")

  val romeo = Seq("Neither, fair saint, if either thee dislike.", "With love's light wings did I o'erperch these walls,\nFor stony limits cannot hold love out,\nAnd what love can do, that dares love attempt:\n Therefore thy kinsmen are no stop to me.", "Alack, there lies more peril in thine eye \nThan twenty of their swords. Look thou but sweet\nAnd I am proof against their enmity.", "By a name\nI know not how to tell thee who I am:\nMy name, dear saint, is hateful to myself,\nBecause it is an enemy to thee.\nHad I it written, I would tear the word.", "I have night's cloak to hide me from their eyes, \nAnd, but thou love me, let them find me here;\nMy life were better ended by their hate\nThan death prorogued, wanting of thy love.", "I take thee at thy word.\n Call me but love, and I'll be new baptis'd;\nHenceforth I never will be Romeo.", "By love, that first did prompt me to enquire.\n He lent me counsel, and I lent him eyes.\n I am no pilot, yet, wert thou as far\nAs that vast shore wash'd with the furthest sea, \nI should adventure for such merchandise.", "[Aside.] Shall I hear more, or shall I speak at this?")
}

