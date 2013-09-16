package infrastructure.api 

import akka.actor._

object EventRouterActor {
  def props(name: String): Props = Props(classOf[EventRouterActor], name)
}

class EventRouterActor(name: String) extends Actor with ActorLogging {
  def receive = {
    case _ => println("Got a message")
  }
}
