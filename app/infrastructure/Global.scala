package infrastructure

import domain._
import play.api.GlobalSettings
import akka.actor.ActorSystem
import com.typesafe.config._
import com.softwaremill.macwire.{InstanceLookup, Macwire}
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent

object Global extends GlobalSettings with Macwire {

  val instanceLookup = InstanceLookup(valsByClass(TardisModule))

  override def getControllerInstance[A](controllerClass: Class[A]) = instanceLookup.lookupSingleOrThrow(controllerClass)
  override def onStart(app: play.api.Application) {
    TardisModule.start(play.libs.Akka.system)
    StatsActors.start(play.libs.Akka.system, TardisModule.chatChannel, TardisModule.clientRepository)
  }
  
  override def onStop(application: play.api.Application) {
    play.libs.Akka.system.shutdown()
  }
}

object TardisModule {
  import com.softwaremill.macwire.MacwireMacros._

  lazy val config = ConfigFactory.load()
  lazy val baseDir = config.getString("tardis.data.dir")
  
  lazy val clientDir = ClientDirectory(baseDir)
  lazy val eventDir = EventDirectory(baseDir)

  lazy val clientRepository = wire[PersistentClientRepository]
  lazy val subscriptionService: SubscriptionService = wire[SubscriptionService]
  lazy val eventRepo = wire[PersistentEventRepository]
  lazy val unackRepository = wire[PersistentUnacknowledgedRepository]
  lazy val pair = Concurrent.broadcast[JsValue]
  lazy val chatOut = pair._1
  lazy val chatChannel = pair._2
  lazy val chatApplication = wire[controllers.Stats]
  
  def start(system: ActorSystem) {
    subscriptionService.start(system)
    val subscriptionActor = system.actorOf(SubscriptionActor.props(clientRepository), name = "subscriber")
    val eventRouterActor = system.actorOf(EventRouterActor.props(subscriptionService, clientRepository, unackRepository, eventRepo), name = "eventrouter")
  }
}

abstract class DataDirectory(str: String, suffix: String) {
  import java.io.File
    
  require(str != null)
  require(!str.endsWith("/"), s"Path to data must not end with a /, but $str does")
  println(s"Suffix$suffix")
  val dir = new File(s"$str$suffix")
  if (!dir.exists) dir.mkdirs()
  assert(dir.exists && dir.isDirectory, s"Directory ${dir.getPath} does not exist or is not a directory!")
  assert(dir.canRead && dir.canWrite, s"Directory ${dir.getPath} cannot be read from and written to!")
  println("Created dir " + dir.getPath)
}

case class ClientDirectory(str: String) extends DataDirectory(str, "/clients/")

case class EventDirectory(str: String) extends DataDirectory(str, "/events")
