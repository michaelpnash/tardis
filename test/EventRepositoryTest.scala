package domain

import org.scalatest.FreeSpec
import com.jglobal.tardis._

import java.util.UUID

class EventRepositoryTest extends FreeSpec {
  "the event repository" - {
    "can store and retrieve an event" in {
      val repo = new EventRepository
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      repo.store(event)
      assert(repo.find(event.id) === Some(event))
    }
  }
}
