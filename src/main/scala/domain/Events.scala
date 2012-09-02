package domain

import org.joda.time._

case class SenderIdentifier(name: String)

object EventIdentifier {
  def apply(ids: String*):EventIdentifier = EventIdentifier(ids.toList)
}
case class EventIdentifier(ids: List[String])

case class EventContainer(event: Array[Byte], senderIdentifier: SenderIdentifier, eventIdentifier: EventIdentifier, timestamp: ReadableInstant)