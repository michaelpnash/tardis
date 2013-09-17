package infrastructure

import play.api.GlobalSettings
import javax.sql.DataSource
import akka.actor.ActorSystem
import controllers.Application
import infrastructure.api._
import domain._

object Global extends GlobalSettings {
  var module: TardisModule = _

  override def getControllerInstance[A](classDef: Class[A]) = module.getController(classDef)

  override def onStart(app: play.api.Application) {
    module = new TardisModule(play.libs.Akka.system)
  }
}

class TardisModule(val system: ActorSystem) {
  import com.softwaremill.macwire.MacwireMacros._

  lazy val application = wire[Application]
  lazy val clientRepository = wire[ClientRepository]

  val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
  val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionActor), name = "eventrouter")
  
  def getController[A](classRef: Class[A]): A = classRef match {
    case x if x.isAssignableFrom(classOf[Application]) => application.asInstanceOf[A]
    case x => throw new IllegalArgumentException("No such controller of class ${classRef.getName} is known")
  }
}
