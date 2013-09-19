package domain

import com.jglobal.tardis._
import java.util.UUID

class EventRepository {
  def store(container: EventContainer): EventContainer = { container }
  def find(id: UUID): Option[EventContainer] = None
}
