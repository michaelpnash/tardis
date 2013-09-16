package infrastructure.api 

import com.jglobal.tardis._
import akka.actor._
import java.util.UUID

object EventRouterActor {
  def props(name: String): Props = Props(classOf[EventRouterActor], name)
}

class EventRouterActor(name: String) extends Actor with ActorLogging {
  def receive = {
    case container: EventContainer => {
      println(s"Got a message: $container")
      sender ! Ack(container.id)
    }
  }
}
