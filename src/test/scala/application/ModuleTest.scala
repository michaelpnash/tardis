package application

import org.scalatest._
import com.google.inject._
import akka.actor._

class ModuleTest extends FunSpec with BeforeAndAfterAll {

  val injector = Guice.createInjector(Stage.PRODUCTION, new Module)
  
  describe("The module")  {
    it ("should produce an actor system") {
      injector.getInstance(classOf[ActorSystem])
    }
  }

  override def afterAll() {
    injector.getInstance(classOf[ActorSystem]).shutdown()
  }
}
