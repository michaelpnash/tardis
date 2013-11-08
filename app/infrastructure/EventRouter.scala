package infrastructure

import com.jglobal.tardis._
import domain._
import akka.actor._
import scala.concurrent.duration._
import akka.agent.Agent

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

  var eventCounter = 0

  val statsActor = context.system.actorSelection("/user/ChatterSupervisor/doctor")
  
  override def preStart() {
    self ! Retry
  }

  def receive = {
    case subscription: Subscription => {
      subscriptionService.subscribe(subscription, sender)
      statsActor ! clientRepo.stats(subscription.clientId)
    }
    case event: EventContainer => {
      eventCounter += 1
      println(s"Event ${eventCounter} received")
      clientRepo.recordPublished(event.clientId, event.eventType)
      clientRepo.subscribersOf(EventType(event.eventType)).foreach(client => {
        unacknowledgedRepo.store(ClientIdAndEventId(client.id, event.id), EventContainerAndTimeStamp(event, System.currentTimeMillis))
        client.sendEvent(event)
        clientRepo.recordEventSent(client.id)
        statsActor ! clientRepo.stats(client.id)
      })
      sender ! Ack(event.id, event.clientId)
      println(s"Ack for event ${eventCounter} was sent")
      statsActor ! clientRepo.stats(event.clientId)
    }
    case stats: ClientStats => {
      println(s"EventRouter Saw request for stats for client ${stats.clientId}")
      subscriptionService.stats(stats, sender)
    }
    case ack: Ack => {
      println(s"Calling recordack for ${ack.clientId}")
      clientRepo.recordAck(ack.clientId)
      unacknowledgedRepo.remove(ClientIdAndEventId(ack.clientId, ack.id))
      statsActor ! clientRepo.stats(ack.clientId)
    }

    case Identify => sender ! ActorIdentity("tardis", Some(self))

    case Retry => {
      unacknowledgedRepo.dueForRetry.foreach(due => due._1.sendEvent(due._2))
      context.system.scheduler.scheduleOnce(30 seconds, self, Retry)(context.dispatcher)
    }
  }

  override def preStart() {
    println("Event Router actor starting")
    super.preStart()
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println(s"Pre-restart: $reason $message")
    super.preRestart(reason, message)
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
     clientRepository.recordSubscription(subscriptionAndSender.sender, subscriptionAndSender.subscription)
     subscriptionAndSender.sender ! "Ok"
   }

   case Flush => {
     clientRepository.flush
     context.system.scheduler.scheduleOnce(20 seconds, self, Flush)(context.dispatcher)
   }
 }
}


