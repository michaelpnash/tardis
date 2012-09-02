package application

import org.scalatest._
import application.InjectorFunSpec
import akka.actor._

class ModuleSpec extends InjectorFunSpec {
  
  describe("The module")  {
    it ("should produce an actor system") {
      injector.getInstance(classOf[ActorSystem])
    }
  }
}
