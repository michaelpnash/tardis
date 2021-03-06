package test

import java.util.UUID
import com.jglobal.tardis._
import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val clientId = args(1)
  val address = "127.0.0.1:9999"
  val proxy: TardisProxy = new TardisProxy(clientId, address)
  val acks = Agent(List[Ack]())
  val ackCount = Agent(0)

  println(s"Running as $clientId")

  val startTime = System.currentTimeMillis

  args(1) match {
    case "test1" => test1()
  }

  println("Test completed in " + (System.currentTimeMillis - startTime) + " ms")
  System.exit(0)

  def test1() {
    val count = 100
    val eventType = "out1"
    val payload = """{token:"value"}"""
    (1 to count).foreach { i => proxy.publish(EventContainer(UUID.randomUUID, eventType, payload, proxy.clientId), ack) }
    wait({ acks().size == count }, 10000)
    require(acks().size == count, "Didn't get expected number of acks, got ${acks().size} but expected ${count}")
  }

  def ack(ack: Ack) {
    ackCount.send(_ + 1)
    println(s"Got publish ack: $ack now ${acks().size}: Ackcount: ${ackCount()}")
    acks.sendOff(_ :+ ack)
  }

  def wait(proc: => Boolean, maxTime: Long) {
    var elapsed = 0
    while(!proc) {
      Thread.sleep(5)
      elapsed += 5
      if (elapsed > maxTime) throw new RuntimeException("Waited too long, aborting")
    }
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
