package infrastructure

import domain._
import org.scalatest.{FreeSpec, BeforeAndAfter}
import com.jglobal.tardis._
import java.util.UUID
import infrastructure.api._
import java.io.File

trait PersistentRepositoryTest {
  def deleteRecursively(dir: File): Boolean = {
    if (dir.isDirectory) dir.listFiles match {
      case null =>
      case xs  => xs foreach deleteRecursively
    }
    dir.delete()
  }
}

class PersistentUnacknowledgedRepositoryTest extends FreeSpec with PersistentRepositoryTest with BeforeAndAfter {
  val path = "/tmp/test"
  before {
    deleteRecursively(new File(path))
  }
  
  "the persistent unacknowledged repository" - {
    "can store and retrieve information for an unacknowledged event to a client" in {
      val eventRepo = new EventRepository
      val repo = new PersistentUnacknowledgedRepository(path, new TransientClientRepository, eventRepo)
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      val clientAndEventId = ClientIdAndEventId("clientId", event.id)
      val containerAndTimeStamp = EventContainerAndTimeStamp(event, System.currentTimeMillis - 40000)
      repo.store(clientAndEventId, containerAndTimeStamp)
      val result = repo.dueForRetry.toList
      assert(result.size === 1)
      assert(result.head._1.id === "clientId")
      assert(result.head._2 === event)
    }
    // "can remove information for an event once it's acknowledged" in {
    //   val eventRepo = new EventRepository
    //   val repo = new PersistentUnacknowledgedRepository(path, new TransientClientRepository, eventRepo, null)
    //   val event = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
    //   eventRepo.store(event)
    //   assert(eventRepo.find(event.id).isDefined)
    //   val clientAndEventId = ClientIdAndEventId("clientId", event.id)
    //   val containerAndTimeStamp = EventContainerAndTimeStamp(event, System.currentTimeMillis - 40000)
    //   repo.store(clientAndEventId, containerAndTimeStamp)
    //   repo.remove(clientAndEventId)
    //   assert(repo.dueForRetry.size === 0)
    // }
    "can store unacknowledged data from one instantiation to another" in {
      val eventRepo = new PersistentEventRepository(path)
      val clientRepo = new PersistentClientRepository(ClientDirectory(path))
      val repo = new PersistentUnacknowledgedRepository(path, clientRepo, eventRepo)
      val event = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      eventRepo.store(event)
      val clientAndEventId = ClientIdAndEventId("clientId", event.id)
      val containerAndTimeStamp = EventContainerAndTimeStamp(event, System.currentTimeMillis - 40000)
      repo.store(clientAndEventId, containerAndTimeStamp)
      
      val eventRepo2 = new PersistentEventRepository(path)
      val clientRepo2 = new PersistentClientRepository(ClientDirectory(path))
      val repo2 = new PersistentUnacknowledgedRepository(path, clientRepo2, eventRepo2)

      val result = repo2.dueForRetry.toList
      assert(result.size === 1)
      assert(result.head._1.id === "clientId")
      assert(result.head._2 === event)      
    }
  }
}
