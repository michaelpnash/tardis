package domain

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import org.joda.time._
import com.mongodb.casbah.commons.conversions.scala._

class EventContainerDataStore {
  RegisterJodaTimeConversionHelpers()

  val mongoConn = MongoConnection("localhost")
  val collection = mongoConn("tardis")("events")

  protected def toDbObject(container: EventContainer) = MongoDBObject("timestamp" -> container.timestamp, 
    "event" -> container.event, "senderIdentifier" -> container.senderIdentifier.name, "eventIdentifier" -> container.eventIdentifier.ids)
  
  protected def fromDbObject(dbobj: DBObject) = EventContainer(timestamp = dbobj.getAs[Instant]("timestamp").get, 
      event = dbobj.getAs[Array[Byte]]("event").get,
      senderIdentifier = SenderIdentifier(dbobj.getAs[String]("senderIdentifier").get),
      eventIdentifier = EventIdentifier(dbobj.getAs[List[String]]("eventIdentifier").get))

  def save(container: EventContainer): EventContainer = {
    collection.save(toDbObject(container))
    container
  }
}