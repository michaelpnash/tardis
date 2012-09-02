package application

import org.scalatest._
import com.google.inject._
import akka.actor._
import com.typesafe.config.ConfigFactory

trait InjectorFunSpec extends FunSpec with BeforeAndAfterAll {

  val injector = Guice.createInjector(Stage.PRODUCTION, new Module {
      override def system: ActorSystem = ActorSystem("tardis", ConfigFactory.load(ConfigFactory.parseString(
          """akka {
               loglevel =DEBUG
  }"""
  )))})

  override def afterAll() {
    injector.getInstance(classOf[ActorSystem]).shutdown()
  }
}