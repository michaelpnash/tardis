package infrastructure

import com.jglobal.tardis._

import org.scalatest.FreeSpec

import java.util.UUID
import java.io.File

class PersistentEventRepositoryTest extends FreeSpec {
  val testDir = "/tmp"
  "the persistent event repository" - {
    "should not instantiate with a path that ends in a /" in {
      intercept[IllegalArgumentException] {
        new PersistentEventRepository("foo/")
      }
    }
    "can store and retrieve an event container across instantiations" in {
      val target = new File(testDir + "/test/")
      target.mkdirs()
      target.delete()
      val repo = new PersistentEventRepository(target.getPath)
      val container2 = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      val container1 = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      repo.store(container1)
      repo.store(container2)
      val repo2 = new PersistentEventRepository(target.getPath)
      assert(repo2.find(container1.id) === Some(container1))
      assert(repo2.find(container2.id) === Some(container2))
    }
  }
}

class SerializableEventContainerTest extends FreeSpec {
  "the serializable event container" - {
    "can turn an event container into a string and back again" in {
      val container = EventContainer(UUID.randomUUID, "type", "payload", "clientId")
      val str = SerializableEventContainer.toStr(container)
      val result = SerializableEventContainer.fromStr(str)
      assert(result === container)
    }
  }
}
