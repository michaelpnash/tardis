package infrastructure.api 

import com.jglobal.tardis._
import domain._
import akka.actor._
import java.util.UUID

object EventRouterActor {
  def props(clientRepository: ClientRepository): Props = Props(classOf[EventRouterActor], clientRepository)
}

class EventRouterActor(clientRepository: ClientRepository) extends Actor with ActorLogging {
  val subscriberActor = context.actorOf(SubscriptionActor.props(clientRepository), name = "subscriptions")
  
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
      println(("*" * 50) + "Got subscription from " + sender.path.address.toString)
      sender ! "Ok" 
    }
  }
}
