import sbt._
import Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import com.typesafe.sbt.osgi.SbtOsgi._
import OsgiKeys._
import org.scalawag.sbt.gitflow.GitFlowPlugin

object TimberBuild extends Build {

  val commonSettings =
    Defaults.defaultSettings ++ osgiSettings ++ GitFlowPlugin.defaults ++ Seq(
      exportJars := true,
      scalacOptions ++= Seq("-unchecked","-deprecation","-feature","-language:implicitConversions","-target:jvm-1.6"),
      javacOptions ++= Seq("-source","1.6","-target","1.6"),
      javaOptions ++= Seq("-Xmx256m","-XX:MaxPermSize=256m"),
      crossScalaVersions := Seq("2.11.5","2.10.2"),
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

  val api =
    Project("timber-api",file("api"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(Dependencies.reflect(scalaVersion.value)),
        exportPackage ++= Seq(
          "org.scalawag.timber.api",
          "org.scalawag.timber.api.impl",
          "org.scalawag.timber.api.style.jul",
          "org.scalawag.timber.api.style.log4j",
          "org.scalawag.timber.api.style.slf4j",
          "org.scalawag.timber.api.style.syslog"
        ),
        importPackage += "org.scalawag.timber.backend;version=\"0.5\""
      )
    )

  val timber =
    Project("timber",file("timber"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(),
        exportPackage ++= Seq(
          "org.scalawag.timber.backend",
          "org.scalawag.timber.dsl",
          "org.scalawag.timber.dsl.debug",
          "org.scalawag.timber.impl",
          "org.scalawag.timber.impl.dispatcher",
          "org.scalawag.timber.impl.factory",
          "org.scalawag.timber.impl.formatter",
          "org.scalawag.timber.impl.receiver"
        )
      )
    ) dependsOn (api)

  val slf4jOverTimber =
    Project("slf4j-over-timber",file("slf4j-over-timber"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(Dependencies.slf4j),
        exportPackage ++= Seq(
          "org.scalawag.timber.bridge.slf4j",
          "org.slf4j.impl;version=1.6.0"
        )
      )
    ) dependsOn (api)

  val timberOverSlf4j =
    Project("timber-over-slf4j",file("timber-over-slf4j"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(Dependencies.slf4j),
        exportPackage += "org.scalawag.timber.backend",
        privatePackage := Seq()
      )
    ) dependsOn (api)

  val logbackSupport =
    Project("timber-logback-support",file("logback-support"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(Dependencies.logback),
        exportPackage ++= Seq(
          "org.scalawag.timber.slf4j.receiver.logback"
        )
      )
    ) dependsOn (timber)

  val examples =
    Project("timber-examples",file("examples"),
      settings = commonSettings
    ) dependsOn (timber,slf4jOverTimber)

  val aggregator = Project("aggregate",file("."),
                           settings = commonSettings ++ Seq(
//                             Keys.`package` := { null },
//                             publishLocal := {},
//                             jacoco.cover := {},
//                             jacoco.report := {},
//                             test := {},
                             publish := {}
                           )) aggregate (api,timber,slf4jOverTimber,timberOverSlf4j,logbackSupport,examples)

  val myResolvers = Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

  override val settings = super.settings ++ Seq(resolvers ++= myResolvers)

  object Dependencies {
    def reflect(scalaVersion:String) = "org.scala-lang" % "scala-reflect" % scalaVersion
    lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1"
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
    lazy val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    lazy val mockito = "org.mockito" % "mockito-all" % "1.9.0" % "test"
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
