import sbt._
import Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object TimberBuild extends Build {
  val VERSION = "0.1-SNAPSHOT"

  val commonSettings =
    Defaults.defaultSettings ++ Seq(
      version := VERSION,
      crossPaths := false,
      scalacOptions ++= Seq("-unchecked","-deprecation"),
      testOptions += Tests.Argument("-oDF"),
      libraryDependencies ++= Seq(Dependencies.scalatest,Dependencies.mockito),
      organization := "org.scalawag.timber"
    ) ++ jacoco.settings ++ Defaults.itSettings

  val timber = Project("timber",file("timber"),settings = commonSettings)

  val slf4jTimber = Project("slf4j-timber",file("slf4j-timber"),
                            settings = commonSettings ++ Seq(
                              libraryDependencies ++= Seq(Dependencies.slf4j)
                            )
                           ) dependsOn (timber)

  val logbackSupport = Project("timber-logback-support",file("logback-support"),
                               settings = commonSettings ++ Seq(
                                 libraryDependencies ++= Seq(Dependencies.logback)
                               )) dependsOn (timber)

  val aggregator = Project("aggregate",file("."),
                           settings = commonSettings ++ Seq(
//                             Keys.`package` := { null },
//                             publishLocal := {},
//                             jacoco.cover := {},
//                             jacoco.report := {},
//                             test := {},
                             publish := {}
                           )) aggregate (timber,slf4jTimber,logbackSupport)

  val myResolvers = Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

  override val settings = super.settings ++ Seq(resolvers ++= myResolvers)

  object Dependencies {
    lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1"
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
    lazy val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    lazy val mockito = "org.mockito" % "mockito-all" % "1.9.0" % "test"
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
