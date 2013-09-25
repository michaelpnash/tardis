package infrastructure

import play.api.GlobalSettings
import javax.sql.DataSource
import akka.actor.ActorSystem
import controllers.Application
import infrastructure.api._
import domain._
import com.typesafe.config._
import com.softwaremill.macwire.{InstanceLookup, Macwire}

object Global extends GlobalSettings with Macwire {

  val instanceLookup = InstanceLookup(valsByClass(TardisModule))

  override def getControllerInstance[A](controllerClass: Class[A]) = instanceLookup.lookupSingleOrThrow(controllerClass)

  override def onStart(app: play.api.Application) {
    TardisModule.start(play.libs.Akka.system)
  }
}

object TardisModule {
  import com.softwaremill.macwire.MacwireMacros._
  var system: ActorSystem = _

  val config = ConfigFactory.load()
  
  lazy val application = wire[Application]
  lazy val eventRepo = wire[EventRepository]
  lazy val clientRepository: ClientRepository = new PersistentClientRepository(config.getString("data.dir"), system)
  lazy val unackRepository: UnacknowledgedRepository = wire[UnacknowledgedRepository]

  def start(playSystem: ActorSystem) {
    system = playSystem
    val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
    val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionActor, clientRepository, unackRepository, eventRepo), name = "eventrouter")
  }  
}
