package domain

import akka.actor.ActorRef

case class Client(id: String, nodes: Set[ClientNode] = Set(), subscribes: Set[EventType] = Set(), publishes: Set[EventType] = Set()) {
  
  val timeout = 30000

  def withNode(node: ClientNode): Client =
    Client(id, nodes.filter(_.ref != node.ref) + node, subscribes, publishes)

  def withoutStaleNodes =
      Client(id, nodes.filter(_.lastSubscription > (System.currentTimeMillis - timeout)), subscribes, publishes)

  def withSubscriptions(eventTypes: List[String]) = Client(id, nodes, subscribes ++ eventTypes.map(et => EventType(et, "")), publishes)

  def withPublishes(eventType: String) = Client(id, nodes, subscribes, publishes + EventType(eventType, ""))
}

case class ClientNode(ref: ActorRef, lastSubscription: Long)
