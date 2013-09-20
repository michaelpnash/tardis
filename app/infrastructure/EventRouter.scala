package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID
import scala.collection.mutable.SynchronizedMap
import scala.concurrent.duration._

case object Retry

object EventRouterActor {
  def props(subscriberActor: ActorRef, clientRepo: ClientRepository,
    unackRepo: UnacknowledgedRepository, eventRepo: EventRepository): Props =
    Props(classOf[EventRouterActor], subscriberActor, clientRepo, unackRepo, eventRepo)
}

case class ClientIdAndEventId(clientId: String, eventId: UUID)
case class EventContainerAndTimeStamp(container: EventContainer, timestamp: Long)

class EventRouterActor(subscriberActor: ActorRef,
  clientRepo: ClientRepository,
  unacknowledgedRepo: UnacknowledgedRepository,
  eventRepo: EventRepository) extends Actor with ActorLogging {

  override def preStart() {
    self ! Retry
  }

  def receive = {
    case subscription: Subscription => subscriberActor forward subscription 
    case event: EventContainer => {
      clientRepo.recordPublished(event.clientId, event.eventType)(context.system)
      clientRepo.subscribersOf(EventType(event.eventType)).foreach(client => {
        unacknowledgedRepo.store(ClientIdAndEventId(client.id, event.id), EventContainerAndTimeStamp(event, System.currentTimeMillis))
        client.sendEvent(event)
      })
      sender ! Ack(event.id, event.clientId)
    }
    case ack: Ack => unacknowledgedRepo.remove(ClientIdAndEventId(ack.clientId, ack.id))

    case Identify => {
      println(s"Got request for identity from $sender")
      sender ! ActorIdentity("tardis", Some(self))
    }

    case Retry => {
      unacknowledgedRepo.dueForRetry.foreach(due => due._1.sendEvent(due._2))
      context.system.scheduler.scheduleOnce(30 seconds, self, Retry)(context.dispatcher)
    }
  }
}

case object Flush

object SubscriptionActor {
  def props(clientRepository: ClientRepository): Props = Props(classOf[SubscriptionActor], clientRepository)
}

class SubscriptionActor(clientRepository: ClientRepository) extends Actor with ActorLogging {

  override def preStart() {
    self ! Flush
  }
  
  def receive = {
   case subscription: Subscription => {
     println(s"Got a subscription from a client: $subscription")
     clientRepository.recordSubscription(sender, subscription)(context.system)
     sender ! "Ok" //TODO: Don't use a string here!
   }

   case Flush => {
     clientRepository.flush
     context.system.scheduler.scheduleOnce(20 seconds, self, Flush)(context.dispatcher)
   }
 }
}

class UnacknowledgedRepository(clientRepo: ClientRepository, system: ActorSystem) {
  val unacknowledged = new collection.mutable.HashMap[ClientIdAndEventId, EventContainerAndTimeStamp] with SynchronizedMap[ClientIdAndEventId, EventContainerAndTimeStamp]

  def store(clientAndEventId: ClientIdAndEventId, containerAndTimeStamp: EventContainerAndTimeStamp) {
    unacknowledged.put(clientAndEventId, containerAndTimeStamp)
  }
  def dueForRetry: Iterable[(Client, EventContainer)] = {
    val minTime = System.currentTimeMillis - 30000
    unacknowledged.filter(_._2.timestamp < minTime).map(pair => {
      unacknowledged.remove(pair._1)
      (clientRepo.findOrCreate(pair._1.clientId)(system), pair._2.container)
    })
  }
  def remove(clientAndEventId: ClientIdAndEventId) {
    unacknowledged.remove(clientAndEventId)
  }
}

// def findFiles(path: File): List[File]  =
//   path :: path.listFiles.filter {
//     _.isDirectory
//   }.toList.flatMap {
//     findFiles(_)
//   }
import java.io.File

class PersistentUnacknowledgedRepository(path: String, clientRepo: ClientRepository, system: ActorSystem) extends UnacknowledgedRepository(clientRepo, system) {
  require(!path.endsWith("/"), s"Path must not end with a /, but $path does")
  val unackDir = new File(path + "/unacknowledged/")
  if (!unackDir.exists) unackDir.mkdirs()
  assert(unackDir.exists && unackDir.isDirectory, s"Directory ${unackDir.getPath} does not exist or is not a directory!")
  assert(unackDir.canRead && unackDir.canWrite, s"Directory ${unackDir.getPath} cannot be read from and written to!")

  initialLoad

  def initialLoad { //TODO: Probably want a limit on how many we attempt to load
    // clients ++= unackDir.listFiles().toList.filter(_.isFile).map(file => {
    //   val client = SerializableClient.fromStr(fromFile(file).getLines.mkString("\n"))(system)
    //   (client.id, client)
    // })
  }

  override def remove(clientAndEventId: ClientIdAndEventId) {
    super.remove(clientAndEventId)
  }
  
  override def store(clientAndEventId: ClientIdAndEventId, containerAndTimeStamp: EventContainerAndTimeStamp) {
    super.store(clientAndEventId, containerAndTimeStamp)
  }
}
