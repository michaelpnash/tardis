package domain

import org.scalatest.FreeSpec

class ClientRepositoryTest extends FreeSpec {
  "a client repository" - {
    "will create a new client with the specified id when finding a client that does not exist" in {
      val repo = new ClientRepository
      val id = "foo"
      val client = repo.findOrCreate(id)
      assert(client.id === id)
    }
    "will return an existing client when it does exist" in {
      val repo = new ClientRepository
      val client = Client("bar", subscribes = List(EventType("name", "descrip")))
      repo.store(client)
      assert(repo.findOrCreate("bar") === client)
    }
    "will overwrite a client with the same id when storing" in {
      val repo = new ClientRepository
      val client = Client("baz", subscribes = List(EventType("name", "descrip")))
      repo.store(client)
      val newBaz = Client("baz", subscribes = List(EventType("other", "some")))
      repo.store(newBaz)
      assert(repo.findOrCreate("baz") === newBaz)
    }
  }
}
