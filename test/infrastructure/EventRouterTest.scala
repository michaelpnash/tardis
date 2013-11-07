package infrastructure

import domain._
import org.scalatest.{FreeSpec, BeforeAndAfterAll}

import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._
import akka.testkit._
import akka.testkit.ImplicitSender
import java.util.UUID

class EventRouterTest(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("test-router", ConfigFactory.parseString(TestKitUsageSpec.config)))
   
  implicit val implSys = system

  private[this] def transientRouter = {
    val clientRepo = new TransientClientRepository
    val subscriptionService = new SubscriptionService(clientRepo)
    val subscriptionActor = TestActorRef(new SubscriptionActor(clientRepo))
    val unacknowledgedRepo = new UnacknowledgedRepository(clientRepo)
    val eventRepo = new EventRepository
    (clientRepo, subscriptionActor, unacknowledgedRepo, eventRepo, TestActorRef(new EventRouterActor(subscriptionService, clientRepo, unacknowledgedRepo, eventRepo)))
  }
  
  "the event router class" - {
    "when receiving an ack" - {
       "increments the number of acks received in the client stats for the client that sent the ack" in {
         val (clientRepo, _, _, _, router) = transientRouter
         val clientId = "clientId"
         val statsBefore = clientRepo.stats(clientId)
         router ! Ack(UUID.randomUUID, clientId)
         val statsAfter = clientRepo.stats(clientId)
         assert(statsAfter.acks.count === statsBefore.acks.count + 1)
       }
       "removes the event corresponding to the ack from the unacknowledged repo" in {
         val (_, _, unackRepo, _, router) = transientRouter
         val clientId = "secondClientId"
         val eventId = UUID.randomUUID
         unackRepo.store(ClientIdAndEventId(clientId, eventId), EventContainerAndTimeStamp(EventContainer(eventId, "type", "payload", clientId), 0L))
         assert(unackRepo.list.size === 1)
         router ! Ack(eventId, clientId)
         assert(unackRepo.list.size === 0)
       }
       "tells the stats actor to send stats for the client id that sent the ack" in {
         //statsActor ! clientRepo.stats(ack.clientId)
       }
     }
    "when receiving a subscription message" - {
//      "updates the appropriate client in the client repository" in {
//        val (clientRepo, _, _, _, router) = transientRouter
//        val id = "someId"
//        val eventType = "someType"
//        router ! Subscription(id, List(eventType))
//        expectMsg("Ok")
//        assert(clientRepo.findOrCreate(id).subscribes === Set(EventType(eventType)))
//      }
    }
    "when receiving a published event" - {
      "records stats for the client indicating another event has been received" in {
        val (clientRepo, _, _, _, router) = transientRouter
        val event = EventContainer(UUID.randomUUID, "type", "payload", "someId")
        router ! event
        expectMsg(Ack(event.id, event.clientId))
        assert(clientRepo.stats(event.clientId).eventsReceivedFrom.count === 1)
      }
      "updates the appropriate client" in {
        val (clientRepo, _, _, _, router) = transientRouter
        val id = "someId"
        val eventType = "someType"
        val event = EventContainer(UUID.randomUUID, eventType, "payload", id)
        router ! event
        expectMsg(Ack(event.id, event.clientId))
        assert(clientRepo.findOrCreate(id).publishes === Set(EventType(eventType)))
      }
      "sends an ack back to the sender of the event with the events id" in {
        val (_, _, _, _, router) = transientRouter
        val id = "someId"
        val eventType = "someType"
        val event = EventContainer(UUID.randomUUID, eventType, "payload", id)
        router ! event
        expectMsg(Ack(event.id, event.clientId))
      }
    }

    "when receiving an ack from a client" - {
      "removes the event from the list of unacknowledged events from that client" in {
        val (_, _, unackRepo, _, router) = transientRouter
        val id = UUID.randomUUID
        val clientId = "id3"
        unackRepo.store(ClientIdAndEventId(clientId, id), EventContainerAndTimeStamp(EventContainer(id, "type", "payload", clientId), 0L))
        router ! Ack(id, clientId)
        assert(unackRepo.list === List())
      }
      "updates the client stats to indicate another ack has been received" in {
        val (clientRepo, _, _, _, router) = transientRouter
        val id = UUID.randomUUID
        val clientId = "id3"
        router ! Ack(id, clientId)
        assert(clientRepo.stats(clientId).acks.count === 1)
      }
    }
  }

  override def afterAll() {
    system.shutdown()
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

