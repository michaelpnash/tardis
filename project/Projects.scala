import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._


object Projects extends Build {

  val globalSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.9.2",
    organization := "JGlobal",
    traceLevel := 10,
    version := "0.0.1"
  )

 /** Application **/

  val akkaVersion = "2.0.1"
  val root = Project(
    "tardis",
    file("."),
    settings = globalSettings ++ assemblySettings ++ Seq(
      test in assembly := {},
      resolvers ++= Seq("typesafe" at "http://repo.typesafe.com/typesafe/releases/"),
      libraryDependencies ++= Seq("com.google.inject" % "guice" % "3.0",
        "com.typesafe.akka"  % "akka-durable-mailboxes" % akkaVersion,
        "com.typesafe.akka"  % "akka-file-mailbox" % akkaVersion,
        "com.typesafe.akka"  % "akka-actor" % akkaVersion,
        "com.typesafe.akka" % "akka-slf4j" % akkaVersion,
        "com.typesafe.akka" % "akka-remote" % akkaVersion,
        "org.specs2" %% "specs2" % "1.12.1" % "test")
    ))
}
