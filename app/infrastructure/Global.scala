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

  def start(system: ActorSystem) {
    val config = ConfigFactory.load()

    lazy val clientRepository: ClientRepository = new PersistentClientRepository(config.getString("tardis.data.dir"), system)
    lazy val application = new Application(clientRepository)
    lazy val eventRepo = new EventRepository
    lazy val unackRepository: UnacknowledgedRepository = new UnacknowledgedRepository(clientRepository, system)
    val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
    val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionActor, clientRepository, unackRepository, eventRepo), name = "eventrouter")
  }
}
