package api

import org.scalatest.FreeSpec

class EventRouterTest extends FreeSpec {
  "the event router class" - {
    "when receiving a client node message" - {
      "adds that client node to the list of known nodes" in {
      }
    }
    "when receiving an event message" - {
      "sends that event to all clients who subscribe to that event type" in {
      }
    }
  }
}
