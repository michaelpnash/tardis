package infrastructure

import play.api.GlobalSettings
import javax.sql.DataSource
import akka.actor.ActorSystem
import controllers.Application
import infrastructure.api._
import domain._
import com.typesafe.config._

object Global extends GlobalSettings {
  var module: TardisModule = _

  override def getControllerInstance[A](classDef: Class[A]) = module.getController(classDef)

  override def onStart(app: play.api.Application) {
    module = new TardisModule(play.libs.Akka.system)
  }
}

class TardisModule(val system: ActorSystem) {
  import com.softwaremill.macwire.MacwireMacros._
  implicit val implSystem = system

  val config = ConfigFactory.load()
  
  lazy val application = wire[Application]
  lazy val eventRepo = wire[EventRepository]
  lazy val clientRepository: ClientRepository = new PersistentClientRepository(config.getString("data.dir"), system)
  lazy val unackRepository: UnacknowledgedRepository = wire[UnacknowledgedRepository]

  val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
  val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionActor, clientRepository, unackRepository, eventRepo), name = "eventrouter")
  
  def getController[A](classRef: Class[A]): A = classRef match {
    case x if x.isAssignableFrom(classOf[Application]) => application.asInstanceOf[A]
    case x => throw new IllegalArgumentException("No such controller of class ${classRef.getName} is known")
  }
}
