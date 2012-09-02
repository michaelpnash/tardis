package domain

import org.joda.time._

trait SenderIdentifier
case class EventContainer(event: Array[Byte], senderIdenfitier: SenderIdentifier, timestamp: Instant)