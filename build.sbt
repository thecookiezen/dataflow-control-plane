import sbt.Keys.version

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.codehaus.janino" % "janino" % "3.1.2",
  "de.siegmar" % "logback-gelf" % "3.0.0",
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.12.3"
)

val baseDependencies = Seq(
  "org.typelevel" %% "cats-effect" % "2.1.3",
  "org.tpolecat"            %% "atto-core"    % "0.7.0",
  "com.softwaremill.quicklens" %% "quicklens" % "1.5.0"
)

val gcpDependecies = Seq(
  "com.google.apis" % "google-api-services-dataflow" % "v1b3-rev20200305-1.30.9",
  "com.google.apis" % "google-api-services-storage" % "v1-rev171-1.25.0",
  "com.google.auth" % "google-auth-library-oauth2-http" % "0.20.0"
)

val scalatest = "org.scalatest" %% "scalatest" % "3.1.2" % Test
val unitTestingStack = Seq(scalatest)

val commonDependencies = baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies ++ gcpDependecies

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.thecookiezen"
ThisBuild / organizationName := "thecookiezen.com"

lazy val root = (project in file("."))
  .settings(
    name := "Dataflow control plane",
    libraryDependencies += commonDependencies
  )
