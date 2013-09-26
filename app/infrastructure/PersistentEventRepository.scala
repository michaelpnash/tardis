package infrastructure

import com.jglobal.tardis.EventContainer
import domain.EventRepository

import java.util.UUID

class PersistentEventRepository extends EventRepository {
  override def store(container: EventContainer): EventContainer = { container }
  override def find(id: UUID): Option[EventContainer] = None
}

