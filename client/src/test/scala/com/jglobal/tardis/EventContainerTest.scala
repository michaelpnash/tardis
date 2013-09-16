package com.jglobal.tardis

import org.scalatest.FreeSpec

import java.util.UUID

class EventContainerTest extends FreeSpec {
  "an event container retains a uuid identifying the event uniquely" in {
    val uuid = UUID.randomUUID
    val container = EventContainer(uuid, "type", "payload", "clientId")
    assert(container.id === uuid)
  }
}
