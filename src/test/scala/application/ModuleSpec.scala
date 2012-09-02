package application

import org.specs2.mutable._
import com.google.inject._
import akka.actor._

class ModuleSpec extends Specification {

  "The module" should {
    "produce an actor system" in new injectors {
      val system = injector.getInstance(classOf[ActorSystem])
      system.name must be("tardis")
    }
  }
}

trait injectors extends After {
  lazy val injector = Guice.createInjector(Stage.PRODUCTION, new Module)
  def after = injector.getInstance(classOf[ActorSystem]).shutdown()
}
