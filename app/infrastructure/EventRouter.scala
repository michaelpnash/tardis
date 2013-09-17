package infrastructure.api 

import com.jglobal.tardis._
import akka.actor._
import java.util.UUID

object EventRouterActor {
  def props(name: String): Props = Props(classOf[EventRouterActor], name)
}

class EventRouterActor(name: String) extends Actor with ActorLogging {
  val subscriberActor = context.actorOf(SubscriptionActor.props("test"), name = "subscriptions")
  
  def receive = {
    case subscription: Subscription => subscriberActor forward subscription 
    case container: EventContainer => {
      println(s"Got a message: $container")
      sender ! Ack(container.id)
    }
  }
}

object SubscriptionActor {
  def props(name: String): Props = Props(classOf[SubscriptionActor], name)
}

class SubscriptionActor(name: String) extends Actor with ActorLogging {
   def receive = {
    case subscription: Subscription => {
      println(("*" * 50) + "Got subscription from " + sender.path.address.toString)
      sender ! "Ok" 
    }
  }
}
