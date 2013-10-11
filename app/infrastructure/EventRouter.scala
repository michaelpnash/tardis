package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID
import scala.concurrent.duration._

case object Retry

object EventRouterActor {
  def props(subscriberActor: ActorRef, clientRepo: ClientRepository,
    unackRepo: UnacknowledgedRepository, eventRepo: EventRepository): Props =
    Props(classOf[EventRouterActor], subscriberActor, clientRepo, unackRepo, eventRepo)
}

class EventRouterActor(subscriberActor: ActorRef,
  clientRepo: ClientRepository,
  unacknowledgedRepo: UnacknowledgedRepository,
  eventRepo: EventRepository) extends Actor with ActorLogging {

  val doctor = context.system.actorSelection("/user/ChatterSupervisor/doctor")
  
  override def preStart() {
    self ! Retry
  }

  def receive = {
    case subscription: Subscription => subscriberActor forward subscription 
    case event: EventContainer => {
      println(s"Sending event to doctor at ${doctor}")
      doctor ! s"I got an event $event"
      clientRepo.recordPublished(event.clientId, event.eventType)(context.system)
      clientRepo.subscribersOf(EventType(event.eventType)).foreach(client => {
        unacknowledgedRepo.store(ClientIdAndEventId(client.id, event.id), EventContainerAndTimeStamp(event, System.currentTimeMillis))
        client.sendEvent(event)
        doctor ! clientRepo.stats(client.id)
      })
      sender ! Ack(event.id, event.clientId)
    }
    case stats: ClientStats => {
      println(s"EventRouter Saw request for stats for client ${stats.clientId}")
      subscriberActor forward stats
    }
    case ack: Ack => {
      doctor ! s"I got an ack: $ack"
      clientRepo.recordAck(ack.clientId)
      unacknowledgedRepo.remove(ClientIdAndEventId(ack.clientId, ack.id))
    }

    case Identify => sender ! ActorIdentity("tardis", Some(self))

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
    case stats: ClientStats => {
      println(s"Sending ${clientRepository.stats(stats.clientId)} to ${sender.path.address}")
      sender ! clientRepository.stats(stats.clientId)
    }
    case subscription: Subscription => {
     clientRepository.recordSubscription(sender, subscription)(context.system)
     sender ! "Ok"
   }

   case Flush => {
     clientRepository.flush
     context.system.scheduler.scheduleOnce(20 seconds, self, Flush)(context.dispatcher)
   }
 }
}


