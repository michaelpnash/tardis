package infrastructure

import akka.ChatActors
import play.api.GlobalSettings
import javax.sql.DataSource
import akka.actor.ActorSystem
import controllers.Application
import domain._
import infrastructure.api.{SubscriptionService, SubscriptionActor}
import com.typesafe.config._
import com.softwaremill.macwire.{InstanceLookup, Macwire}
import infrastructure.api.EventRouterActor
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent

object Global extends GlobalSettings with Macwire {

  val instanceLookup = InstanceLookup(valsByClass(TardisModule))

  override def getControllerInstance[A](controllerClass: Class[A]) = instanceLookup.lookupSingleOrThrow(controllerClass)
  override def onStart(app: play.api.Application) {
    TardisModule.start(play.libs.Akka.system)
    ChatActors.start(play.libs.Akka.system, TardisModule.chatChannel)
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
  lazy val subscriptionService: SubscriptionService = wire[SubscriptionService]
  lazy val application = wire[Application]
  lazy val eventRepo = wire[EventRepository]
  lazy val unackRepository = wire[UnacknowledgedRepository]
  lazy val pair = Concurrent.broadcast[JsValue]
  lazy val chatOut = pair._1;
  lazy val chatChannel = pair._2;
  lazy val chatApplication = wire[controllers.ChatApplication];
  
  def start(system: ActorSystem) {
    subscriptionService.start(system)
    val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
    val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionService, clientRepository, unackRepository, eventRepo), name = "eventrouter")
  }
}
