package test

import java.util.UUID
import com.jglobal.tardis._

object Main extends App {

  val clientId = args(0)
  val address = "127.0.0.1:9999"
  val proxy: TardisProxy = new TardisProxy(clientId, address)

  println(s"Running $clientId")
  System.exit(0)

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

  }
}
