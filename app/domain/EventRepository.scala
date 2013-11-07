package domain

import com.jglobal.tardis._
import java.util.UUID

class EventRepository {
  protected val events = new collection.mutable.HashMap[UUID, EventContainer] with collection.mutable.SynchronizedMap[UUID, EventContainer]
  def store(container: EventContainer): EventContainer = {
    events.put(container.id, container)
    container
  }
  def find(id: UUID): Option[EventContainer] = events.get(id)
}
