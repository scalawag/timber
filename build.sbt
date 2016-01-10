// timber -- Copyright 2012-2015 -- Justin Patterson
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.typesafe.sbt.SbtSite
import com.typesafe.sbt.osgi.OsgiKeys._
import com.typesafe.sbt.site.JekyllSupport
import sbt._
import scoverage._
import org.scalawag.sbt.gitflow.GitFlowPlugin
import SiteKeys._

lazy val commonSettings = GitFlowPlugin.defaults ++ Seq(
  organization := "org.scalawag.timber",
  scalaVersion := "2.11.7",
  exportJars := true,
  scalacOptions ++= Seq("-unchecked","-deprecation","-feature","-language:implicitConversions","-target:jvm-1.6"),
  javacOptions ++= Seq("-source","1.6","-target","1.6"),
//  testOptions += Tests.Argument("-oDF"),
  coverageEnabled in Test := true,

  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },

  homepage := Some(url("http://scalawag.org/timber")),
  startYear := Some(2012),
  licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  scmInfo := Some(ScmInfo(url("http://github.com/scalawag/timber"),"scm:git:git://github.com/scalawag/timber.git")),
  developers := List(Developer("justinp","Justin Patterson","justin@scalawag.org",url("https://github.com/justinp"))),

  libraryDependencies ++= Seq (
    "org.scalatest" %% "scalatest" % "2.2.4",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2"
  ) map ( _ % "test" )

) ++ osgiSettings ++ site.settings

val timberApi = project.in(file("timber-api")).settings(commonSettings:_*).settings(
  name := "timber-api",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ),
  exportPackage ++= Seq(
    "org.scalawag.timber.api",
    "org.scalawag.timber.api.impl",
    "org.scalawag.timber.api.style.jul",
    "org.scalawag.timber.api.style.log4j",
    "org.scalawag.timber.api.style.slf4j",
    "org.scalawag.timber.api.style.syslog"
  ),
  importPackage += "org.scalawag.timber.backend;version=\"0.5\""
).settings(site.includeScaladoc():_*)

val timberBackend = project.in(file("timber-backend")).settings(commonSettings:_*).settings(
  name := "timber-backend",
  exportPackage += "org.scalawag.timber.backend.*"
).settings(site.includeScaladoc():_*) dependsOn (timberApi)

val slf4jOverTimber = project.in(file("slf4j-over-timber")).settings(commonSettings:_*).settings(
  name := "slf4j-over-timber",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1",
  exportPackage ++= Seq(
    "org.scalawag.timber.bridge.slf4j",
    "org.slf4j.impl;version=1.6.0"
  )
) dependsOn (timberApi)

val timberOverSlf4j = project.in(file("timber-over-slf4j")).settings(commonSettings:_*).settings(
  name := "timber-over-slf4j",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1",
  exportPackage ++= Seq(
    "org.scalawag.timber.backend"
  )
) dependsOn (timberApi)

val logbackSupport = project.in(file("timber-logback-support")).settings(commonSettings:_*).settings(
  name := "timber-logback-support",
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.7"
  ),
  exportPackage ++= Seq(
    "org.scalawag.timber.slf4j.receiver.logback"
  )
) dependsOn (timberBackend)

val testProjectSettings = commonSettings ++ Seq(
  fork in Test := true,
  publishArtifact := false
)

val examples = project.settings(commonSettings:_*).settings(
  name := "timber-examples",
  publishArtifact := false
) dependsOn (timberBackend,slf4jOverTimber)

val DebugModeTest =
  project.in(file("tests/DebugMode")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.debug")
    ) dependsOn (timberBackend)

val SpecifiedDispatcherTest =
  project.in(file("tests/SpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.ThrowingDispatcher")
    ) dependsOn (timberBackend)

val CantCastSpecifiedDispatcherTest =
  project.in(file("tests/CantCastSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.NotReallyADispatcher")
    ) dependsOn (timberBackend)

val CantFindSpecifiedDispatcherTest =
  project.in(file("tests/CantFindSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.MissingDispatcher")
    ) dependsOn (timberBackend)

val CantInstantiateSpecifiedDispatcherTest =
  project.in(file("tests/CantInstantiateSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.UnloadableClass")
    ) dependsOn (timberBackend)

val RuntimeSpecifiedDispatcherTest =
  project.in(file("tests/RuntimeSpecifiedDispatcher")).settings(testProjectSettings:_*) dependsOn (timberBackend)

val CloseOnShutdownTest =
  project.in(file("tests/CloseOnShutdown")).settings(testProjectSettings:_*) dependsOn (timberBackend)

val CloseOnSignalTest =
  project.in(file("tests/CloseOnSignal")).settings(testProjectSettings:_*) dependsOn (timberBackend)

val root = project.in(file(".")).
  settings(site.settings:_*).
  settings(site.jekyllSupport():_*).
  settings(ghpages.settings:_*).
  settings(
    aggregate in update := false,
    publishArtifact := false,
    publishTo := Some(Resolver.file("Not actually used but required by publish-signed", file("/tmp/bogusrepo"))),
    siteMappings ++= siteMappings.in(timberApi).value.map { case (f,t) => (f,t.replace("latest/api","docs/timber-api")) },
    siteMappings ++= siteMappings.in(timberBackend).value.map { case (f,t) => (f,t.replace("latest/api","docs/timber-backend")) },
    siteMappings ++= (sourceDirectory.in(JekyllSupport.Jekyll).value ** "*.svg") pair relativeTo(sourceDirectory.in(JekyllSupport.Jekyll).value),
    git.remoteRepo := "https://github.com/scalawag/timber.git"
  ).
  aggregate(
    timberApi,
    timberBackend,
    slf4jOverTimber,
    timberOverSlf4j,
    logbackSupport,
    examples,
    DebugModeTest,
    SpecifiedDispatcherTest,
    CantCastSpecifiedDispatcherTest,
    CantFindSpecifiedDispatcherTest,
    CantInstantiateSpecifiedDispatcherTest,
    RuntimeSpecifiedDispatcherTest,
    CloseOnShutdownTest,
    CloseOnSignalTest
  )
