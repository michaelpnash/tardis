package infrastructure

import domain._
import org.scalatest.FreeSpec
import com.jglobal.tardis._
import java.util.UUID
import infrastructure.api._

class PersistentUnacknowledgerRepositoryTest extends FreeSpec {
  val path = "/tmp"
  "the persistent unacknowledged repository" - {
    "can store and retrieve information for an unacknowledged event to a client" in {
      val eventRepo = new EventRepository
      val repo = new PersistentUnacknowledgedRepository(path, new TransientClientRepository, eventRepo, null)
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      val clientAndEventId = ClientIdAndEventId("clientId", event.id)
      val containerAndTimeStamp = EventContainerAndTimeStamp(event, System.currentTimeMillis - 40000)
      repo.store(clientAndEventId, containerAndTimeStamp)
      val result = repo.dueForRetry.toList
      assert(result.size === 1)
    }
    "can list all information for unacknowledged events that are due for retry" in {
    }
    "can remove information for an event once it's acknowledged" in {
    }
    "can store unacknowledged data from one instantiation to another" in {
      
    }
  }
}
