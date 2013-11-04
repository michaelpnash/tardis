import sbt._
import play.Project._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "tardis"
  val appVersion      = "1.0-SNAPSHOT"
  val akkaVersion     = "2.2.0"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "jglobal.com",
    version      := appVersion,
    scalaVersion := "2.10.3",
    // make sure that the artifacts don't have the scala version in the name
    crossPaths   := false,
    resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots/"
  )

  val client = Project(id = "client", base = file("client"),
    settings = buildSettings ++
      Seq(libraryDependencies ++= Dependencies.client))
  
  val main = play.Project(appName, appVersion, Dependencies.base).settings(
    sbt.Keys.fork in Test := false
  ).dependsOn(client)

  lazy val integration = Project(
    id = "integration",
    base = file("integration"),
    settings = buildSettings ++
      Seq(libraryDependencies ++= Dependencies.base)
  ).dependsOn(main)
  
  object Dependencies {
    val base = Seq(
      "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
      "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
      "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
      "com.softwaremill.macwire" %% "core" % "0.4",
      "joda-time" % "joda-time" % "2.1",
      "org.joda" % "joda-convert" % "1.2",
      "org.webjars" %% "webjars-play" % "2.2.0-RC1-1",
      "org.webjars" % "angularjs" % "1.1.5-1",
      "org.webjars" % "bootstrap" % "2.3.2",
      // ---- application dependencies ----
      "com.typesafe.akka"  %% "akka-remote" % akkaVersion,
      
      // ---- test dependencies ----
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test"
    )

    val client = Seq("org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
      "com.typesafe.akka"  %% "akka-remote" % akkaVersion,
      "com.typesafe.akka"  %% "akka-actor" % akkaVersion,
      "com.typesafe.akka"  %% "akka-testkit" % akkaVersion)
  }
}
