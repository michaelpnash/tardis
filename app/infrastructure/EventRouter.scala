package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID
import scala.collection.mutable.SynchronizedMap

object EventRouterActor {
  def props(subscriberActor: ActorRef, clientRepo: ClientRepository): Props = Props(classOf[EventRouterActor], subscriberActor, clientRepo)
}

class EventRouterActor(subscriberActor: ActorRef, clientRepo: ClientRepository) extends Actor with ActorLogging {
  val unacknowledged = new collection.mutable.HashMap[(String, UUID), EventContainer] with SynchronizedMap[(String, UUID), EventContainer]

  def receive = {
    case subscription: Subscription => subscriberActor forward subscription 
    case event: EventContainer => {
      println(s"++++++++++++++++++++++++++++++ Got event $event")
      clientRepo.recordPublished(event.clientId, event.eventType)(context.system)
      sender ! Ack(event.id, event.clientId)
      clientRepo.subscribersOf(EventType(event.eventType)).foreach(client => {
        unacknowledged.put((client.id, event.id), event)
        client.sendEvent(event)
      })
    }
    case ack: Ack => {}
    case Identify => sender ! ActorIdentity("tardis", Some(self))
  }
}

object SubscriptionActor {
  def props(clientRepository: ClientRepository): Props = Props(classOf[SubscriptionActor], clientRepository)
}

class SubscriptionActor(clientRepository: ClientRepository) extends Actor with ActorLogging {
   def receive = {
    case subscription: Subscription => {
      println(s"Got a subscription from a client: $subscription")
      clientRepository.recordSubscription(sender, subscription)(context.system)
      sender ! "Ok" //TODO: Don't use a string here!
    }
  }
}


