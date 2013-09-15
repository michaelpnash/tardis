package infrastructure

import play.api.GlobalSettings
import javax.sql.DataSource
import akka.actor.ActorSystem
import controllers.Application

object Global extends GlobalSettings {
  val module = new TardisModule

  override def getControllerInstance[A](classDef: Class[A]) = module.getController(classDef)
}

class TardisModule {
  import com.softwaremill.macwire.MacwireMacros._

  val actorSystem = ActorSystem("tardis")
  lazy val application = wire[Application]

  def getController[A](classRef: Class[A]): A = classRef match {
    case x if x.isAssignableFrom(classOf[Application]) => application.asInstanceOf[A]
    case x => throw new IllegalArgumentException("No such controller of class ${classRef.getName} is known")
  }
}
