package com.jglobal.tardis

case class CountAndLast(count: Long, last: Long) {
  def increment = CountAndLast(count + 1, System.currentTimeMillis)
}

case class ClientStats(clientId: String, eventsSentTo: CountAndLast = CountAndLast(0, 0),
  acks: CountAndLast = CountAndLast(0, 0), eventsReceivedFrom: CountAndLast = CountAndLast(0, 0)) {
  
  def withSentEvent = ClientStats(clientId, eventsSentTo.increment, acks, eventsReceivedFrom)
  def withAck = ClientStats(clientId, eventsSentTo, acks.increment, eventsReceivedFrom)
  def withReceivedEvent = ClientStats(clientId, eventsSentTo, acks, eventsReceivedFrom.increment)
}
