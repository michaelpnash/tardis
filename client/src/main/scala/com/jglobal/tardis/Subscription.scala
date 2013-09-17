package com.jglobal.tardis

case class Subscription(clientId: String, eventTypes: List[String]) {
  require(eventTypes.size > 0)
}

