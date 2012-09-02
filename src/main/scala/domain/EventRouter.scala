package domain

import akka.actor._

class EventRouter extends Actor with ActorLogging {
  def receive = {
    case _ => throw new RuntimeException("Not implemented")
  }
}