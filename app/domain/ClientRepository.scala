package domain

import akka.actor._ //TODO: Shouldn't be here in domain...
import com.jglobal.tardis._
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

trait ClientRepository {

  def list: Iterable[Client]
  
  def findOrCreate(id: String): Client

  def store(client: Client): Client
  
  def recordSubscription(ref: ActorRef, subscription: Subscription)(implicit system: ActorSystem): Client

  def recordPublished(clientId: String, publishedType: String)(implicit system: ActorSystem): Client
  
  def subscribersOf(eventType: EventType): Iterable[Client]

  def flush: Unit

  def stats(clientId: String): ClientStats

  def recordAck(clientId: String): Unit

  def recordEventReceived(clientId: String): Unit

  def recordEventSent(clientId: String): Unit
}

class TransientClientRepository extends ClientRepository {
  protected val clients = new collection.mutable.HashMap[String, Client] with SynchronizedMap[String, Client]
  protected val clientStats = new collection.mutable.HashMap[String, ClientStats] with SynchronizedMap[String, ClientStats]

  def list = clients.values
  
  def findOrCreate(id: String) = clients.get(id).getOrElse({
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

  def recordPublished(clientId: String, publishedType: String)(implicit system: ActorSystem): Client = {
    recordEventReceived(clientId)
    store(findOrCreate(clientId).withPublishes(publishedType))
  }

  def subscribersOf(eventType: EventType) = clients.values.filter(_.subscribes.contains(eventType))

  def flush {
    clients.map(p => clients.put(p._1, p._2.withoutStaleNodes))
  }

  def stats(clientId: String) = clientStats.get(clientId).getOrElse(ClientStats(clientId))

  def recordAck(clientId: String) {
    clientStats.put(clientId, stats(clientId).withAck)
  }

  def recordEventReceived(clientId: String) {
    clientStats.put(clientId, stats(clientId).withReceivedEvent)
  }

  def recordEventSent(clientId: String) {
    clientStats.put(clientId, stats(clientId).withSentEvent)
  }
}


