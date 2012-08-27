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

  val root = Project(
    "tardis",
    file("."),
    settings = globalSettings ++ assemblySettings ++ Seq(
      test in assembly := {},
      resolvers ++= Seq("typesafe" at "http://repo.typesafe.com/typesafe/releases/"),
      libraryDependencies ++= Seq("com.typesafe.akka" % "akka-remote" % "2.0.1")
    ))
}
