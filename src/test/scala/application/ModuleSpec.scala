package application

import org.specs2.mutable._
import com.google.inject._

class ModuleSpec extends Specification {

  "The module" should {
    "produce an injector without error" in {
      val injector = Guice.createInjector(Stage.PRODUCTION, new Module)
      injector must not be null
    }
  }
}
