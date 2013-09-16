package client
import akka.actor.ActorSystem
import play.api.test._
import play.api.test.Helpers._

import org.scalatest.FreeSpec

class BusMultiJvmNode1 extends FreeSpec {
  "a thing" - {
    "should do a different thing" in {
      val clientSystem = ActorSystem("client")
      val bus = clientSystem.actorSelection("akka.tcp://application@10.64.103.6:2552")
      bus ! "test"
      
    }
  }
}

class BusMultiJvmNode2 extends FreeSpec {
  "a thing" - {
    "should do a thing" in {
      running(FakeApplication()) {
      }
    }
  }
}
