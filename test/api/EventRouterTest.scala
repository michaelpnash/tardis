package api

import org.scalatest.FreeSpec

class EventRouterTest extends FreeSpec {
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
}
