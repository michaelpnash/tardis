package domain

import akka.actor._
import com.jglobal.tardis._
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.io.Source._
import infrastructure._
import java.io.PrintWriter

trait ClientRepository {

  def list: Iterable[Client]
  
  def findOrCreate(id: String)(implicit system: ActorSystem): Client

  def store(client: Client): Client
  
  def recordSubscription(ref: ActorRef, subscription: Subscription)(implicit system: ActorSystem): Client

  def recordPublished(clientId: String, publishedType: String)(implicit system: ActorSystem): Client
  
  def subscribersOf(eventType: EventType): Iterable[Client]

  def flush: Unit
}

class TransientClientRepository extends ClientRepository {
  protected val clients = new collection.mutable.HashMap[String, Client] with SynchronizedMap[String, Client]

  def list = clients.values
  
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

  def flush {
    clients.map(p => clients.put(p._1, p._2.withoutStaleNodes))
  }
}

import java.io.File

class PersistentClientRepository(path: String, system: ActorSystem) extends TransientClientRepository {
  require(!path.endsWith("/"), s"Path must not end with a /, but $path does")
  val clientDir = new File(path + "/clients/")
  if (!clientDir.exists) clientDir.mkdirs()
  assert(clientDir.exists && clientDir.isDirectory, s"Directory ${clientDir.getPath} does not exist or is not a directory!")
  assert(clientDir.canRead && clientDir.canWrite, s"Directory ${clientDir.getPath} cannot be read from and written to!")

  initialLoad

  def initialLoad {
    clients ++= clientDir.listFiles().toList.filter(_.isFile).map(file => {
      val client = SerializableClient.fromStr(fromFile(file).getLines.mkString("\n"))(system)
      (client.id, client)
    })
  }
  
  override def store(client: Client): Client = {
    Some(new PrintWriter(s"${clientDir.getPath}/${client.id}")).foreach{p => p.write(SerializableClient.toStr(client)); p.close}

    super.store(client)
  }
}




