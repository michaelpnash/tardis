package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID
import scala.concurrent.duration._

case object Retry

object EventRouterActor {
  def props(subscriptionService: SubscriptionService, clientRepo: ClientRepository,
    unackRepo: UnacknowledgedRepository, eventRepo: EventRepository): Props =
    Props(classOf[EventRouterActor], subscriptionService, clientRepo, unackRepo, eventRepo)
}

class EventRouterActor(subscriptionService: SubscriptionService,
  clientRepo: ClientRepository,
  unacknowledgedRepo: UnacknowledgedRepository,
  eventRepo: EventRepository) extends Actor with ActorLogging {

  val doctor = context.system.actorSelection("/user/ChatterSupervisor/doctor")
  
  override def preStart() {
    self ! Retry
  }

  def receive = {
    case subscription: Subscription => subscriptionService.subscribe(subscription, sender)
    case event: EventContainer => {
      println(s"Sending event to doctor at ${doctor}")
      doctor ! s"I got an event $event"
      clientRepo.recordPublished(event.clientId, event.eventType)(context.system)
      clientRepo.subscribersOf(EventType(event.eventType)).foreach(client => {
        unacknowledgedRepo.store(ClientIdAndEventId(client.id, event.id), EventContainerAndTimeStamp(event, System.currentTimeMillis))
        client.sendEvent(event)
        doctor ! clientRepo.stats(client.id)
      })
      doctor ! clientRepo.stats(event.clientId)
      sender ! Ack(event.id, event.clientId)
    }
    case stats: ClientStats => {
      println(s"EventRouter Saw request for stats for client ${stats.clientId}")
      subscriptionService.stats(stats, sender)
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

case class StatsAndSender(stats: ClientStats, sender: ActorRef)
case class SubscriptionAndSender(subscription: Subscription, sender: ActorRef)

class SubscriptionService(clientRepository: ClientRepository) {
  var actor: ActorRef = _

  def subscribe(subscription: Subscription, sender: ActorRef) = actor ! SubscriptionAndSender(subscription, sender)

  def stats(stats: ClientStats, sender: ActorRef) = actor ! StatsAndSender(stats, sender)
  
  def start(system: ActorSystem) {
    println("Starting subscription actor")
    actor = system.actorOf(SubscriptionActor.props(clientRepository), "subscription")
  }
}

object SubscriptionActor {
  def props(clientRepository: ClientRepository): Props = Props(classOf[SubscriptionActor], clientRepository)
}

class SubscriptionActor(clientRepository: ClientRepository) extends Actor with ActorLogging {

  override def preStart() {
    self ! Flush
  }
  
  def receive = {
    case statsAndSender: StatsAndSender => {
      println(s"Sending ${clientRepository.stats(statsAndSender.stats.clientId)} to ${statsAndSender.sender.path.address}")
      statsAndSender.sender ! clientRepository.stats(statsAndSender.stats.clientId)
    }
    case subscriptionAndSender: SubscriptionAndSender => {
     clientRepository.recordSubscription(subscriptionAndSender.sender, subscriptionAndSender.subscription)(context.system)
     subscriptionAndSender.sender ! "Ok"
   }

   case Flush => {
     clientRepository.flush
     context.system.scheduler.scheduleOnce(20 seconds, self, Flush)(context.dispatcher)
   }
 }
}


