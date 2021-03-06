package com.jglobal.tardis

import java.util.UUID

object Main extends App {

  var proxy: TardisProxy = _
  var clientId = "commandline"

  var lastSent: Option[EventContainer] = None
  
  println("Interactive TARDIS client. ? for Help")
  print(">")
  for( ln <- io.Source.stdin.getLines ) {
    ln.split(" ").toList match {
      case "?" :: tail => println("""Help
This is an interactive client app to the TARDIS event router. You can type the following commands:
    ?                 Show this help message
    connect host:port clientId  Connect to the TARDIS server at the given host and port, with the specified client Id (default for 127.0.0.1:9999, commandline)
    subscribe xxx     Subscribe to event type xxx. Print the events to the console as they are received
    publish xxx yyyy  Publish event type xxx, with the remainder of the line being the payload of the event (e.g. yyyy could be some json)
    repeat nnn        Repeat the last publish command nnn times
    exit              Exit this command loop
""")
      case "connect" :: address :: id :: Nil => println(connect(address, id))
      case "connect" :: address :: Nil => println(connect(address))
      case "connect" :: Nil => println(connect("127.0.0.1:9999"))
      case "publish" :: eventType :: payload :: Nil => println(publish(eventType, payload))
      case "subscribe" :: eventType :: Nil => println(subscribe(eventType))
      case "repeat" :: count :: Nil => println(repeat(count))
      case "stats" :: Nil => println(stats)
      case "exit" :: tail => System.exit(0)
      case _ => println("Unknown command. Enter ? for help")
    }
    print(">")
  }

  def connect(address: String): String = {
    proxy = new TardisProxy(clientId, address)
    s"Connected as $clientId"
  }

  def connect(address: String, id: String): String = {
    clientId = id
    connect(address)
  }

  def stats = {
    proxy.stats("commandline")
  }

  def subscribe(eventType: String) = {
    proxy.registerHandler({
      evt => println(evt)
      proxy.ack(evt.id)
    }, eventType)
    s"Subscribed to $eventType"
  }

  def publish(eventType: String, payload: String) = {
    val evt = EventContainer(UUID.randomUUID, eventType, payload, proxy.clientId)
    proxy.publish(evt, { ack: Ack => if (ack.id == evt.id) println("Publish confirmed") else println("Error publishing!") })
    lastSent = Some(evt)
    "Event sent"
  }

  def repeat(count: String) = {
    lastSent match {
      case Some(evt) => for (i <- 1 to count.toInt) publish(evt.eventType, evt.payload)
      case None => "No event to repeat"
    }
  }
}

