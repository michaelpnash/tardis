import sbt._
import sbt._
import play.Project._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm }

object ApplicationBuild extends Build {

  val appName         = "tardis"
  val appVersion      = "1.0-SNAPSHOT"
  val akkaVersion     = "2.2.0"

  lazy val buildSettings = Defaults.defaultSettings ++ multiJvmSettings ++ Seq(
    organization := "jglobal.com",
    version      := appVersion,
    scalaVersion := "2.10.2",
    // make sure that the artifacts don't have the scala version in the name
    crossPaths   := false,
    resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots/"
  )

  val main = play.Project(appName, appVersion, Dependencies.base).settings(
    sbt.Keys.fork in Test := false
  ) configs(MultiJvm)

  val client = play.Project("client", appVersion, Dependencies.base,
    path = file("client")
  ).settings(
    sbt.Keys.fork in Test := false
  ) 

  val integration = play.Project("integration", appVersion, Dependencies.base,
    path = file("integration")
  ).dependsOn(main, client).settings() configs(MultiJvm)

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((_, testResults), (_, multiJvmResults))  =>
          val results = testResults ++ multiJvmResults
          (Tests.overall(results.values), results)
      }
  )

  object Dependencies {
    val base = Seq(
      "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
      "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
      "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
      "com.softwaremill.macwire" %% "core" % "0.1",
      "joda-time" % "joda-time" % "2.1",
      "org.joda" % "joda-convert" % "1.2",

      // ---- application dependencies ----
      "com.typesafe.akka"  %% "akka-remote" % akkaVersion,
      
      // ---- test dependencies ----
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test"
    )
  }
}