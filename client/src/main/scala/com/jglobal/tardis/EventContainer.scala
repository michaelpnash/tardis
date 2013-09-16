package com.jglobal.tardis

import java.util.UUID

case class EventContainer(id: UUID, eventType: String, payload: String, clientId: String)
