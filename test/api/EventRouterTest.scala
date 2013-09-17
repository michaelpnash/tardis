package api

import domain.ClientRepository
import org.scalatest.{FreeSpec, BeforeAndAfterAll}

import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._
import akka.testkit._
import akka.testkit.ImplicitSender
import infrastructure.api._

class EventRouterTest(system: ActorSystem) extends TestKit(system) with FreeSpec with ImplicitSender with BeforeAndAfterAll {

  def this() = this(ActorSystem("test-client-repo", ConfigFactory.load().getConfig("test").withFallback(ConfigFactory.load())))

  implicit val implSys = system
  
  "the event router class" - {
    "when receiving a subscription message" - {
      "updates the appropriate client in the client repository" in {
        val clientRepo = new ClientRepository
        val router = TestActorRef(new EventRouterActor(clientRepo))
      }
    }
    "when receiving a published event" - {
      "updates the appropriate client" in {
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
