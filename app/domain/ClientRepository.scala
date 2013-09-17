package domain

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

class ClientRepository {
  val clients = new collection.mutable.HashMap[String, Client] with SynchronizedMap[String, Client]
  def findOrCreate(id: String) = clients.get(id).getOrElse({
    val client = Client(id)
    clients.put(client.id, client)
    client
  })
  def store(client: Client): Client = {
    clients.put(client.id, client)
    client
  }
}
