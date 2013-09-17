package domain

import akka.actor.ActorRef

case class Client(id: String, nodes: Set[ClientNode] = Set(), subscribes: List[EventType] = List(), publishes: List[EventType] = List()) {
  val timeout = 30000
  def withNode(node: ClientNode): Client = 
    Client(id, nodes.filter(_.ref != node.ref) + node, subscribes, publishes)
  def withoutStaleNodes = 
      Client(id, nodes.filter(_.lastSubscription > (System.currentTimeMillis - timeout)), subscribes, publishes)
}

case class ClientNode(ref: ActorRef, lastSubscription: Long)
