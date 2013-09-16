package client
import akka.actor.ActorSystem

import org.scalatest.FreeSpec

class BusMultiJvmClient extends FreeSpec {
  "a thing" - {
    "should do a different thing" in {
      val clientSystem = ActorSystem("client")
    }
  }
}

class BusMultiJvmServer extends FreeSpec {
  "a thing" - {
    "should do a thing" in {
      infrastructure.Global.module
    }
  }
}
