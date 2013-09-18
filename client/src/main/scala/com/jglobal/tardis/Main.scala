package com.jglobal.tardis

object Main extends App {
  println("Interactive TARDIS client. ? for Help")
  print(">")
  for( ln <- io.Source.stdin.getLines ) {
    ln.split(" ").head match {
      case "?" => println("""Help
This is an interactive client app to the TARDIS event router. You can type the following commands:
    ?                 Show this help message
    connect host:port Connect to the TARDIS server at the given host and port (default for 127.0.0.1:9000)
    subscribe xxx     Subscribe to event type xxx. Print the events to the console as they are received
    publish xxx yyyy  Publish event type xxx, with the remainder of the line being the payload of the event (e.g. yyyy could be some json)
    exit              Exit this command loop
""")
      case "exit" => System.exit(0)
      case _ => println("Unknown command. Enter ? for help")
    }
    print(">")
  }
}

