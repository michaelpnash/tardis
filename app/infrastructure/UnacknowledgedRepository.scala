package domain

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID
import scala.collection.mutable.SynchronizedMap
import scala.concurrent.duration._

case class ClientIdAndEventId(clientId: String, eventId: UUID)
case class EventContainerAndTimeStamp(container: EventContainer, timestamp: Long)

class UnacknowledgedRepository(clientRepo: ClientRepository) {
  val unacknowledged = new collection.mutable.HashMap[ClientIdAndEventId, EventContainerAndTimeStamp] with SynchronizedMap[ClientIdAndEventId, EventContainerAndTimeStamp]

  def list = unacknowledged.keys.toList
  
  def store(clientAndEventId: ClientIdAndEventId, containerAndTimeStamp: EventContainerAndTimeStamp) {
    unacknowledged.put(clientAndEventId, containerAndTimeStamp)
  }
  def dueForRetry: Iterable[(Client, EventContainer)] = {
    val minTime = System.currentTimeMillis - 30000
    unacknowledged.filter(_._2.timestamp < minTime).map(pair => {
      unacknowledged.remove(pair._1)
      (clientRepo.findOrCreate(pair._1.clientId), pair._2.container)
    })
  }
  def remove(clientAndEventId: ClientIdAndEventId) {
    unacknowledged.remove(clientAndEventId)
  }
}
