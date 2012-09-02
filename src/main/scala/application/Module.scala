package application

import com.google.inject.{Inject, Provides, AbstractModule, Singleton}
import akka.actor._
import com.mongodb.casbah.MongoConnection
import org.joda.time._
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb._

class Module extends AbstractModule {

  @Provides @Singleton def system: ActorSystem = ActorSystem("tardis")

  @Provides @Singleton def db: DB = {
    val mongo = new Mongo("localhost").getDB("tardis")
    mongo
  }

  def configure() {

  }
}