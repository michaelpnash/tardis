package domain

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import org.joda.time._

class EventContainerDataStore {
  val mongoConn = MongoConnection("localhost")
  val collection = mongoConn("tardis")("events")
  def save(container: EventContainer): EventContainer = {

    val dbobj = MongoDBObject("timestamp" -> container.timestamp)
    collection.save(dbobj)
    container
  }
}