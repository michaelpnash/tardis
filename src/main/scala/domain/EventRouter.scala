package domain

import akka.actor._
import com.google.inject.{Singleton, Inject}

@Singleton
class EventRouter @Inject()(eventRepository: EventRepository) extends Actor with ActorLogging {
  def receive = {
    case _ => throw new RuntimeException("Not implemented")
  }
}