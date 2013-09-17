package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID

object EventRouterActor {
  def props(subscriberActor: ActorRef): Props = Props(classOf[EventRouterActor], subscriberActor)
}

class EventRouterActor(subscriberActor: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    case subscription: Subscription => subscriberActor forward subscription 
    case container: EventContainer => {
      println(s"Got a message: $container")
      sender ! Ack(container.id)
    }
  }
}

object SubscriptionActor {
  def props(clientRepository: ClientRepository): Props = Props(classOf[SubscriptionActor], clientRepository)
}

class SubscriptionActor(clientRepository: ClientRepository) extends Actor with ActorLogging {
   def receive = {
    case subscription: Subscription => {
      println("Recording subscription" * 10)
      clientRepository.recordSubscription(sender, subscription)
      sender ! "Ok" 
    }
  }
}
