package client
import akka.actor.ActorSystem

import org.scalatest.FreeSpec

class SpecMultiJvmNode1 extends FreeSpec {
  "a thing" - {
    "should do a different thing" in {
      println("akka.remote.port system property is: " + System.getProperty("akka.remote.port"))
      System.setProperty("akka.remote.port", "8888")
      val clientSystem = ActorSystem("client")
      val bus = clientSystem.actorSelection("akka.tcp://application@10.64.103.6:9999")
      bus ! "test"
      
    }
  }
}

class SpecMultiJvmNode2 extends FreeSpec {
  "a thing" - {
    "should do a thing" in {
      //running(FakeApplication()) {
      //}
    }
  }
}
