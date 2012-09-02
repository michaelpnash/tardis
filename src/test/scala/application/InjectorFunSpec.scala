package application

import org.scalatest._
import com.google.inject._
import akka.actor._
import com.typesafe.config.ConfigFactory
import com.foursquare.fongo.Fongo
import com.mongodb.DB

trait InjectorFunSpec extends FunSpec with BeforeAndAfterAll {

  val injector = Guice.createInjector(Stage.PRODUCTION, new Module {
      override def system: ActorSystem = ActorSystem("tardis", 
        ConfigFactory.load(ConfigFactory.parseString("akka { loglevel =DEBUG}")))
      override def db: DB = {
        val mongo = new Fongo("localhost").getDB("tardis")
        mongo
      }
    })

  override def afterAll() {
    injector.getInstance(classOf[ActorSystem]).shutdown()
  }
}