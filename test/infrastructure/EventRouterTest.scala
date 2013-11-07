package infrastructure

import org.scalatest.FreeSpec
import infrastructure.api._
import akka.testkit.{TestActorRef, TestKit, DefaultTimeout, ImplicitSender}
import domain._
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorRef, ActorSystem}
import scala.collection.immutable
import com.jglobal.tardis.{EventContainer, Ack}
import java.util.UUID
import domain.ClientIdAndEventId

class EventRouterTest extends TestKit(ActorSystem("TestKitUsageSpec",
    ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout with ImplicitSender with FreeSpec {

  val subscriptionService = null
  val clientRepo = new TransientClientRepository()
  val unackRepo = new UnacknowledgedRepository(clientRepo)
  val eventRepo = null
  val eventRouter = TestActorRef(new EventRouterActor(subscriptionService, clientRepo,
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
        val clientId = "secondClientId"
        val eventId = UUID.randomUUID
        unackRepo.store(ClientIdAndEventId(clientId, eventId), EventContainerAndTimeStamp(EventContainer(eventId, "type", "payload", clientId), 0L))
        assert(unackRepo.list.size === 1)
        eventRouter ! Ack(eventId, clientId)
        assert(unackRepo.list.size === 0)
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
}
