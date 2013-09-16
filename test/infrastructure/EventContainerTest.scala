package infrastructure

import org.scalatest.FreeSpec

import java.util.UUID

class EventContainerTest extends FreeSpec {
  "an event container retains a uuid identifying the event uniquely" in {
    val uuid = UUID.randomUUID
    val container = EventContainer(uuid, "type", "payload")
    assert(container.id === uuid)
  }
  "an event container can be created from appropriate json" in {
    val uuid = UUID.randomUUID
    val json = s"""{"id":"$uuid","type":"type","payload":"payload"}"""
    assert(EventContainer.fromJSON(json) === EventContainer(uuid, "type", "payload"))
  }
  "appropriate JSON can be converted into the corresponding event container" in {
    val container = EventContainer(UUID.randomUUID, "type", "payload")
    assert(EventContainer.toJSON(container) === s"""{"id":"${container.id}","type":"type","payload":"payload"}""")
  }
}
