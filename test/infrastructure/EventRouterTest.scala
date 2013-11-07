package infrastructure

import org.scalatest.FreeSpec
import infrastructure.api._
import akka.testkit.{TestKit, DefaultTimeout, ImplicitSender}
import domain.{TransientClientRepository, EventRepository, UnacknowledgedRepository}
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorRef, ActorSystem}
import scala.collection.immutable
import com.jglobal.tardis.Ack
import java.util.UUID

class EventRouterTest extends TestKit(ActorSystem("TestKitUsageSpec",
    ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout with ImplicitSender with FreeSpec {

  val subscriptionService = null
  val clientRepo = new TransientClientRepository()
  val unackRepo = new UnacknowledgedRepository(clientRepo)
  val eventRepo = null
  val eventRouter = system.actorOf(EventRouterActor.props(subscriptionService, clientRepo,
      unackRepo, eventRepo))
  "the event router actor" - {
    "when receiving an ack" - {
      "increments the number of acks received in the client stats for the client that sent the ack" in {
        val clientId = "clientId"
        val statsBefore = clientRepo.stats(clientId)
        eventRouter ! Ack(UUID.randomUUID, clientId)
        val statsAfter = clientRepo.stats(clientId)
        assert(statsAfter.acks.count === statsBefore.acks.count + 1)
      }
      "removes the event corresponding to the ack from the unacknowledged repo" in {
        //unacknowledgedRepo.remove(ClientIdAndEventId(ack.clientId, ack.id))
      }
      "tells the stats actor to send stats for the client id that sent the ack" in {
        //statsActor ! clientRepo.stats(ack.clientId)
      }
    }
  }
}

object TestKitUsageSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

  /**
   * An Actor that echoes everything you send to it
   */
  class EchoActor extends Actor {
    def receive = {
      case msg ⇒ sender ! msg
    }
  }

  /**
   * An Actor that forwards every message to a next Actor
   */
  class ForwardingActor(next: ActorRef) extends Actor {
    def receive = {
      case msg ⇒ next ! msg
    }
  }

  /**
   * An Actor that only forwards certain messages to a next Actor
   */
  class FilteringActor(next: ActorRef) extends Actor {
    def receive = {
      case msg: String ⇒ next ! msg
      case _           ⇒ None
    }
  }

  /**
   * An actor that sends a sequence of messages with a random head list, an
   * interesting value and a random tail list. The idea is that you would
   * like to test that the interesting value is received and that you cant
   * be bothered with the rest
   */
  class SequencingActor(next: ActorRef, head: immutable.Seq[String],
                        tail: immutable.Seq[String]) extends Actor {
    def receive = {
      case msg ⇒ {
        head foreach { next ! _ }
        next ! msg
        tail foreach { next ! _ }
      }
    }
  }
}
