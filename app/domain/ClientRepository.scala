package domain

import akka.actor._
import com.jglobal.tardis._

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
  def recordSubscription(ref: ActorRef, subscription: Subscription): Client = 
    store(
      findOrCreate(subscription.clientId).withNode(ClientNode(ref, System.currentTimeMillis)).withSubscriptions(subscription.eventTypes))

  def recordPublished(clientId: String, publishedType: String): Client =
    store(
      findOrCreate(clientId).withPublishes(publishedType)
    )  
}
