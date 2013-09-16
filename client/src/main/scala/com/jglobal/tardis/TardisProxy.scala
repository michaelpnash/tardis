package com.jglobal.tardis

import java.util.UUID

class TardisProxy {
  def publish(evt: EventContainer, confirm: (Ack) => Unit) {}
  def registerHandler(handler: (EventContainer) => Unit, eventType: String) {}
  def ack(id: UUID) {}
}
