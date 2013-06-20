import sbt._
import Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object TimberBuild extends Build {
  val VERSION = "0.2-SNAPSHOT"

  val commonSettings =
    Defaults.defaultSettings ++ Seq(
      version := VERSION,
      crossPaths := false,
      exportJars := true,
      scalacOptions ++= Seq("-unchecked","-deprecation","-feature","-language:implicitConversions"),
      javaOptions ++= Seq("-Xmx256m","-XX:MaxPermSize=256m"),
      scalaVersion := "2.10.0",
      testOptions += Tests.Argument("-oDF"),
      libraryDependencies ++= Seq(Dependencies.scalatest,Dependencies.mockito),
      organization := "org.scalawag.timber",
      publishMavenStyle := true,
      publishArtifact in Test := false,
      publishTo <<= version { (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      pomIncludeRepository := { _ => false },
      pomExtra :=
        <url>http://scalwag.org/timber</url>
        <licenses>
          <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>http://github.com/scalawag/timber</url>
          <connection>scm:git:git://github.com/scalawag/timber.git</connection>
        </scm>
        <developers>
          <developer>
            <id>justinp</id>
            <name>Justin Patterson</name>
            <email>justin@scalawag.org</email>
            <url>https://github.com/justinp</url>
          </developer>
        </developers>
    ) ++ jacoco.settings ++ Defaults.itSettings

  val timber = Project("timber",file("timber"),
                       settings = commonSettings ++ Seq(
                         libraryDependencies ++= Seq(Dependencies.actor)
                       )
                      )

  val slf4jTimber = Project("slf4j-timber",file("slf4j-timber"),
                            settings = commonSettings ++ Seq(
                              libraryDependencies ++= Seq(Dependencies.slf4j)
                            )
                           ) dependsOn (timber)

  val logbackSupport = Project("timber-logback-support",file("logback-support"),
                               settings = commonSettings ++ Seq(
                                 libraryDependencies ++= Seq(Dependencies.logback)
                               )) dependsOn (timber)
  val api =
    Project("timber-api",file("api"),
      settings = commonSettings
  )

  val examples = Project("timber-examples",file("examples"),
                         settings = commonSettings
                        ) dependsOn (timber,slf4jTimber)

  val aggregator = Project("aggregate",file("."),
                           settings = commonSettings ++ Seq(
//                             Keys.`package` := { null },
//                             publishLocal := {},
//                             jacoco.cover := {},
//                             jacoco.report := {},
//                             test := {},
                             publish := {}
                           )) aggregate (timber,slf4jTimber,logbackSupport,api)

  val myResolvers = Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

  override val settings = super.settings ++ Seq(resolvers ++= myResolvers)

  object Dependencies {
    lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1"
    lazy val actor = "com.typesafe.akka" %% "akka-actor" % "2.1.0"
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
    lazy val scalatest = "org.scalatest" %% "scalatest" % "1.9" % "test"
    lazy val mockito = "org.mockito" % "mockito-all" % "1.9.0" % "test"
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
