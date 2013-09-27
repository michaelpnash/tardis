package api

import domain._
import org.scalatest.{FreeSpec, BeforeAndAfterAll}

import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._
import akka.testkit._
import akka.testkit.ImplicitSender
import infrastructure.api._
import java.util.UUID

class EventRouterTest(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("test-router", ConfigFactory.load().getConfig("test")))
   
  implicit val implSys = system

  private[this] def transientRouter = {
    val clientRepo = new TransientClientRepository
    val subscriptionActor = TestActorRef(new SubscriptionActor(clientRepo))
    val unacknowledgedRepo = new UnacknowledgedRepository(clientRepo, system)
    val eventRepo = new EventRepository
    (clientRepo, subscriptionActor, unacknowledgedRepo, eventRepo, TestActorRef(new EventRouterActor(subscriptionActor, clientRepo, unacknowledgedRepo, eventRepo)))
  }
  
  "the event router class" - {
    "when receiving a subscription message" - {
      "updates the appropriate client in the client repository" in {
        val (clientRepo, _, _, _, router) = transientRouter
        val id = "someId"
        val eventType = "someType"
        router ! Subscription(id, List(eventType))
        expectMsg("Ok")
        assert(clientRepo.findOrCreate(id).subscribes === Set(EventType(eventType)))
      }
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
    system.shutdown
  }
}
