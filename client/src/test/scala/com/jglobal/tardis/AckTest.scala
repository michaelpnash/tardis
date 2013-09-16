package com.jglobal.tardis

import org.scalatest.FreeSpec

import java.util.UUID

class AckTest extends FreeSpec {
  "an ack retains a uuid identifying the event it is acknowledging" in {
    val uuid = UUID.randomUUID
    val ack = Ack(uuid)
    assert(ack.id === uuid)
  }
  "an ack can be created from appropriate json" in {
    val uuid = UUID.randomUUID
    val json = s"""{"id":"$uuid"}"""
    assert(Ack.fromJSON(json) === Ack(uuid))
  }
  "appropriate JSON can be converted into the corresponding ack" in {
    val ack = Ack(UUID.randomUUID)
    assert(Ack.toJSON(ack) === s"""{"id":"${ack.id}"}""")
  }
}
