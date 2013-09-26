package infrastructure

import com.jglobal.tardis._

import org.scalatest.FreeSpec

import java.util.UUID

class PersistentEventRepositoryTest extends FreeSpec {
  "the persistent event repository" - {
    "can store and retrieve an event container across instantiations" in {
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
