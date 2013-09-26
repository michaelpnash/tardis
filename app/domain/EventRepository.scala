package domain

import com.jglobal.tardis._
import java.util.UUID
import scala.collection.mutable.{HashMap, SynchronizedMap}

class EventRepository {
  protected val events = new collection.mutable.HashMap[UUID, EventContainer] with SynchronizedMap[UUID, EventContainer]
  def store(container: EventContainer): EventContainer = {
    events.put(container.id, container)
    container }
  def find(id: UUID): Option[EventContainer] = events.get(id)
}
