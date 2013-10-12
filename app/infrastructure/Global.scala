package infrastructure

import akka.ChatActors
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
    ChatActors.start(play.libs.Akka.system)
  }
  
  override def onStop(application: play.api.Application) {
    play.libs.Akka.system.shutdown()
  }
}

object TardisModule {
  import com.softwaremill.macwire.MacwireMacros._

  lazy val config = ConfigFactory.load()
  lazy val clientDir = ClientDirectory(config.getString("tardis.data.dir"))
  lazy val clientRepository = wire[PersistentClientRepository]
  lazy val subscriptionService = wire[SubscriptionService]
  lazy val application = wire[Application]
  lazy val eventRepo = wire[EventRepository]
  lazy val unackRepository = wire[UnacknowledgedRepository]
  
  def start(system: ActorSystem) {
    val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
    val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionActor, clientRepository, unackRepository, eventRepo), name = "eventrouter")
    //subscriptionService.start(system)
  }
}
