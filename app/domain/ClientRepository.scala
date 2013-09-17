package domain

import akka.actor._
import com.jglobal.tardis._

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

class ClientRepository {
  val clients = new collection.mutable.HashMap[String, Client] with SynchronizedMap[String, Client]
  def findOrCreate(id: String)(implicit system: ActorSystem) = clients.get(id).getOrElse({
    val client = Client(id)
    clients.put(client.id, client)
    client
  })
  def store(client: Client): Client = {
    clients.put(client.id, client)
    client
  }
  def recordSubscription(ref: ActorRef, subscription: Subscription)(implicit system: ActorSystem): Client = 
    store(
      findOrCreate(subscription.clientId).withNode(ClientNode(ref, System.currentTimeMillis)).withSubscriptions(subscription.eventTypes))

  def recordPublished(clientId: String, publishedType: String)(implicit system: ActorSystem): Client =
    store(
      findOrCreate(clientId).withPublishes(publishedType)
    )

  def subscribersOf(eventType: EventType) = clients.values.filter(_.subscribes.contains(eventType))
}










