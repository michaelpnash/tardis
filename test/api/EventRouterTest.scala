package api

import domain.{ClientRepository, EventType}
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
    //.withFallback(ConfigFactory.load())))

  implicit val implSys = system
  
  "the event router class" - {
    "when receiving a subscription message" - {
      "updates the appropriate client in the client repository" in {
        val clientRepo = new ClientRepository
        val subscriptionActor = TestActorRef(new SubscriptionActor(clientRepo))
        val router = TestActorRef(new EventRouterActor(subscriptionActor, clientRepo))
        val id = "someId"
        val eventType = "someType"
        router ! Subscription(id, List(eventType))
        expectMsg("Ok")
        assert(clientRepo.findOrCreate(id).subscribes === Set(EventType(eventType)))
      }
    }
    "when receiving a published event" - {
      "updates the appropriate client" in {
        val clientRepo = new ClientRepository
        val subscriptionActor = TestActorRef(new SubscriptionActor(clientRepo))
        val router = TestActorRef(new EventRouterActor(subscriptionActor, clientRepo))
        val id = "someId"
        val eventType = "someType"
        val event = EventContainer(UUID.randomUUID, eventType, "payload", id)
        router ! event
        expectMsg(Ack(event.id))
        assert(clientRepo.findOrCreate(id).publishes === Set(EventType(eventType)))
      }
      "sends an ack back to the sender of the event with the events id" in {
        val clientRepo = new ClientRepository
        val subscriptionActor = TestActorRef(new SubscriptionActor(clientRepo))
        val router = TestActorRef(new EventRouterActor(subscriptionActor, clientRepo))
        val id = "someId"
        val eventType = "someType"
        val event = EventContainer(UUID.randomUUID, eventType, "payload", id)
        router ! event
        expectMsg(Ack(event.id))
      }
      "sends the event to all subscriber clients" in {
      }
    }
    "when receiving an ack" - {
    }
  }

  override def afterAll() {
    system.shutdown
  }
}
