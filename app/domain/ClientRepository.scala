package domain

import akka.actor._ //TODO: Shouldn't be here in domain...
import com.jglobal.tardis._
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

trait ClientRepository {

  def list: Iterable[Client]
  
  def findOrCreate(id: String): Client

  def store(client: Client): Client
  
  def recordSubscription(ref: ActorRef, subscription: Subscription): Client

  def recordPublished(clientId: String, publishedType: String): Client
  
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
  
  override def findOrCreate(id: String) = clients.get(id).getOrElse({
    val client = Client(id)
    clients.put(client.id, client)
    client
  })

  override def store(client: Client): Client = {
    clients.put(client.id, client)
    client
  }
  override def recordSubscription(ref: ActorRef, subscription: Subscription): Client =
    store(
      findOrCreate(subscription.clientId).withNode(ClientNode(ref, System.currentTimeMillis)).withSubscriptions(subscription.eventTypes))

  override def recordPublished(clientId: String, publishedType: String): Client = {
    recordEventReceived(clientId)
    store(findOrCreate(clientId).withPublishes(publishedType))
  }

  override def subscribersOf(eventType: EventType) = clients.values.filter(_.subscribes.contains(eventType))

  override def flush {
    clients.map(p => clients.put(p._1, p._2.withoutStaleNodes))
  }

  override def stats(clientId: String) = clientStats.get(clientId).getOrElse(ClientStats(clientId))

  override def recordAck(clientId: String) {
    clientStats.put(clientId, stats(clientId).withAck)
  }

  override def recordEventReceived(clientId: String) {
    clientStats.put(clientId, stats(clientId).withReceivedEvent)
  }

  override def recordEventSent(clientId: String) {
    clientStats.put(clientId, stats(clientId).withSentEvent)
  }
}


