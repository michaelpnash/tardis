package domain

case class Client(id: String, nodes: Set[ClientNode] = Set(), subscribes: List[EventType] = List(), publishes: List[EventType] = List())

case class ClientNode(host: String, port: Int)
