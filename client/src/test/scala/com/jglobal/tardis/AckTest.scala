package com.jglobal.tardis

import org.scalatest.FreeSpec

import java.util.UUID

class AckTest extends FreeSpec {
  "an ack retains a uuid identifying the event it is acknowledging" in {
    val uuid = UUID.randomUUID
    val ack = Ack(uuid, "clientId")
    assert(ack.id === uuid)
  }
}
