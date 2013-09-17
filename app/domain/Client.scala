package domain

import com.jglobal.tardis._
import akka.actor._
import akka.routing.RoundRobinRouter

case class Client(id: String, nodes: Set[ClientNode] = Set(), subscribes: Set[EventType] = Set(), publishes: Set[EventType] = Set())(implicit system: ActorSystem) {
  
  val timeout = 30000

  lazy private[this] val router = system.actorOf(Props.empty.withRouter(
    RoundRobinRouter(routees = nodes.map(_.ref))))

  def withNode(node: ClientNode): Client =
    Client(id, nodes.filter(_.ref != node.ref) + node, subscribes, publishes)

  def withoutStaleNodes =
      Client(id, nodes.filter(_.lastSubscription > (System.currentTimeMillis - timeout)), subscribes, publishes)

  def withSubscriptions(eventTypes: List[String]) = Client(id, nodes, subscribes ++ eventTypes.map(et => EventType(et, "")), publishes)

  def withPublishes(eventType: String) = Client(id, nodes, subscribes, publishes + EventType(eventType, ""))

  def sendEvent(container: EventContainer): EventContainer = {
    println(s"Sending event $container to ${nodes.size} nodes")
    router ! container
    container
  }
}

case class ClientNode(ref: ActorRef, lastSubscription: Long)
