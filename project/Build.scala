import sbt._
import sbt._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "tardis"
  val appVersion      = "1.0-SNAPSHOT"
  val akkaVersion     = "2.1.0"

  val appDependencies = Seq(
    "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
    "com.softwaremill.macwire" %% "core" % "0.1",
    "com.typesafe.akka"  %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    sbt.Keys.fork in Test := false
  )

  val client = play.Project("client", appVersion,
    Seq("com.typesafe.slick" %% "slick" % "1.0.1",
      "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
      "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"
    ),
    path = file("client")
  ).settings(
    sbt.Keys.fork in Test := false
  )
}
