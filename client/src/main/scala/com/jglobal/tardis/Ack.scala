package com.jglobal.tardis

import java.util.UUID
import scala.util.parsing.json._

object Ack {
  def fromJSON(str: String): Ack = {
    val json: Option[Any] = JSON.parseFull(str)
    val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    new Ack(UUID.fromString(map("id").toString))
  }
  def toJSON(ack: Ack): String = s"""{"id":"${ack.id}"}"""
}

case class Ack(id: UUID)
