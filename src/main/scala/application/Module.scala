package application

import com.google.inject.{Inject, Provides, AbstractModule, Singleton}
import akka.actor._

class Module extends AbstractModule {

  @Provides @Singleton def system: ActorSystem = ActorSystem("tardis")

  def configure() {

  }
}