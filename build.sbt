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

import com.typesafe.sbt.osgi.OsgiKeys._
import sbt._
import scoverage._
import org.scalawag.sbt.gitflow.GitFlowPlugin

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

) ++ osgiSettings ++ site.settings ++ site.includeScaladoc()

val api = project.in(file("api")).settings(commonSettings:_*).settings(
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
)

val timber = project.settings(commonSettings:_*).settings(
  name := "timber",
  exportPackage += "org.scalawag.timber.backend.*"
) dependsOn (api)

val slf4jOverTimber = project.in(file("slf4j-over-timber")).settings(commonSettings:_*).settings(
  name := "slf4j-over-timber",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1",
  exportPackage ++= Seq(
    "org.scalawag.timber.bridge.slf4j",
    "org.slf4j.impl;version=1.6.0"
  )
) dependsOn (api)

val timberOverSlf4j = project.in(file("timber-over-slf4j")).settings(commonSettings:_*).settings(
  name := "timber-over-slf4j",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1",
  exportPackage ++= Seq(
    "org.scalawag.timber.backend"
  )
) dependsOn (api)

val logbackSupport = project.in(file("logback-support")).settings(commonSettings:_*).settings(
  name := "timber-logback-support",
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.7"
  ),
  exportPackage ++= Seq(
    "org.scalawag.timber.slf4j.receiver.logback"
  )
) dependsOn (timber)

val testProjectSettings = commonSettings ++ Seq(
  fork in Test := true,
  publishArtifact := false
)

val examples = project.settings(commonSettings:_*).settings(
  name := "timber-examples",
  publishArtifact := false
) dependsOn (timber,slf4jOverTimber)

val DebugModeTest =
  project.in(file("tests/DebugMode")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.debug")
    ) dependsOn (timber)

val SpecifiedDispatcherTest =
  project.in(file("tests/SpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.ThrowingDispatcher")
    ) dependsOn (timber)

val CantCastSpecifiedDispatcherTest =
  project.in(file("tests/CantCastSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.NotReallyADispatcher")
    ) dependsOn (timber)

val CantFindSpecifiedDispatcherTest =
  project.in(file("tests/CantFindSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.MissingDispatcher")
    ) dependsOn (timber)

val CantInstantiateSpecifiedDispatcherTest =
  project.in(file("tests/CantInstantiateSpecifiedDispatcher")).settings(testProjectSettings:_*).
    settings(
      javaOptions in Test := Seq("-Dtimber.dispatcher.class=test.UnloadableClass")
    ) dependsOn (timber)

val RuntimeSpecifiedDispatcherTest =
  project.in(file("tests/RuntimeSpecifiedDispatcher")).settings(testProjectSettings:_*) dependsOn (timber)

val CloseOnShutdownTest =
  project.in(file("tests/CloseOnShutdown")).settings(testProjectSettings:_*) dependsOn (timber)

val CloseOnSignalTest =
  project.in(file("tests/CloseOnSignal")).settings(testProjectSettings:_*) dependsOn (timber)

val root = project.in(file(".")).settings(
  aggregate in update := false,
  publishArtifact := false,
  publishTo := Some(Resolver.file("Not actually used but required by publish-signed", file("/tmp/bogusrepo")))
).aggregate(
  api,
  timber,
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
