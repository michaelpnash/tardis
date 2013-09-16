package infrastructure.api 

import akka.actor._

object EventRouterActor {
  def props(name: String): Props = Props(classOf[EventRouterActor], name)
}

class EventRouterActor(name: String) extends Actor with ActorLogging {
  def receive = {
    case x: Any => {
      println(s"Got a message: $x")
      sender ! "ack"
    }
  }
}
