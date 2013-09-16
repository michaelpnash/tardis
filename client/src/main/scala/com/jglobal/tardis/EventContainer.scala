package com.jglobal.tardis

import java.util.UUID
import scala.util.parsing.json._

object EventContainer {
  val idTag = "id"
  val typeTag = "type"
  val payloadTag = "payload"
  def fromJSON(str: String): EventContainer = {
    val json: Option[Any] = JSON.parseFull(str)
    val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    new EventContainer(UUID.fromString(map("id").toString), map("type").toString, map("payload").toString)
  }
  def toJSON(evt: EventContainer): String = s"""{"id":"${evt.id}","type":"${evt.eventType}","payload":"${evt.payload}"}"""
}

case class EventContainer(id: UUID, eventType: String, payload: String)
