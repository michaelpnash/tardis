package api

import org.scalatest.{FreeSpec, BeforeAndAfterAll}

import akka.actor._
import com.jglobal.tardis._
import com.typesafe.config._

class EventRouterTest extends FreeSpec with BeforeAndAfterAll {
  val config = ConfigFactory.load()
  val system = ActorSystem("test-client-repo", config.getConfig("test").withFallback(config))
  "the event router class" - {
    "when receiving a subscription message" - {
      "updates the appropriate client in the client repository" in {
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
