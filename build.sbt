// timber -- Copyright 2012-2021 -- Justin Patterson
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
//import com.typesafe.sbt.site.JekyllSupport
import sbt._
import scoverage._
//import org.scalawag.sbt.gitflow.GitFlowPlugin
//import SiteKeys._

lazy val commonSettings = /*GitFlowPlugin.defaults ++*/ Seq(
  organization := "org.scalawag.timber",
  version := "0.7.0-pre.2",
  scalaVersion := "2.13.5",
  crossScalaVersions := Seq("2.12.13", "2.13.5"),
  exportJars := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
//  testOptions += Tests.Argument("-oDF"),
  Test / coverageEnabled := true,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },
  homepage := Some(url("http://scalawag.org/timber")),
  startYear := Some(2012),
  licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  scmInfo := Some(ScmInfo(url("http://github.com/scalawag/timber"), "scm:git:git://github.com/scalawag/timber.git")),
  developers := List(
    Developer("justinp", "Justin Patterson", "justin@scalawag.org", url("https://github.com/justinp"))
  ),
  credentials += Credentials("GnuPG Key ID", "gpg", "439444E02ED9335F91C538455283F6A358FB8629", "ignored"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.8",
    "org.scalamock" %% "scalamock" % "5.1.0"
  ) map (_ % "test")
) //++ site.settings

val timberApi = project
  .in(file("timber-api"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-api",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
  ) //.settings(site.includeScaladoc():_*)

val timberBackend = project
  .in(file("timber-backend"))
  .dependsOn(timberApi)
  .settings(commonSettings: _*)
  .settings(
    name := "timber-backend"
  )
//  .settings(site.includeScaladoc():_*)

val slf4jOverTimber = project
  .in(file("slf4j-over-timber"))
  .settings(commonSettings: _*)
  .settings(
    name := "slf4j-over-timber",
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1"
  ) dependsOn (timberApi)

val timberOverSlf4j = project
  .in(file("timber-over-slf4j"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-over-slf4j",
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1",
  ) dependsOn (timberApi)

val logbackSupport = project
  .in(file("timber-logback-support"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-logback-support",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.7"
    ),
  ) dependsOn (timberBackend)

val testProjectSettings = commonSettings ++ Seq(
  Test / fork := true,
  publishArtifact := false
)

val examples = project
  .settings(commonSettings: _*)
  .settings(
    name := "timber-examples",
    publishArtifact := false
  ) dependsOn (timberBackend, slf4jOverTimber)

val DebugModeTest =
  project
    .in(file("tests/DebugMode"))
    .settings(testProjectSettings: _*)
    .settings(
      Test / javaOptions := Seq("-Dtimber.debug")
    ) dependsOn (timberBackend)

val SpecifiedDispatcherTest =
  project
    .in(file("tests/SpecifiedDispatcher"))
    .settings(testProjectSettings: _*)
    .settings(
      Test / javaOptions := Seq("-Dtimber.dispatcher.class=test.ThrowingDispatcher")
    ) dependsOn (timberBackend)

val CantCastSpecifiedDispatcherTest =
  project
    .in(file("tests/CantCastSpecifiedDispatcher"))
    .settings(testProjectSettings: _*)
    .settings(
      Test / javaOptions := Seq("-Dtimber.dispatcher.class=test.NotReallyADispatcher")
    ) dependsOn (timberBackend)

val CantFindSpecifiedDispatcherTest =
  project
    .in(file("tests/CantFindSpecifiedDispatcher"))
    .settings(testProjectSettings: _*)
    .settings(
      Test / javaOptions := Seq("-Dtimber.dispatcher.class=test.MissingDispatcher")
    ) dependsOn (timberBackend)

val CantInstantiateSpecifiedDispatcherTest =
  project
    .in(file("tests/CantInstantiateSpecifiedDispatcher"))
    .settings(testProjectSettings: _*)
    .settings(
      Test / javaOptions := Seq("-Dtimber.dispatcher.class=test.UnloadableClass")
    ) dependsOn (timberBackend)

val RuntimeSpecifiedDispatcherTest =
  project.in(file("tests/RuntimeSpecifiedDispatcher")).settings(testProjectSettings: _*) dependsOn (timberBackend)

val CloseOnShutdownTest =
  project.in(file("tests/CloseOnShutdown")).settings(testProjectSettings: _*) dependsOn (timberBackend)

val CloseOnSignalTest =
  project.in(file("tests/CloseOnSignal")).settings(testProjectSettings: _*) dependsOn (timberBackend)

val timber = project
  .in(file("."))
  .
//  settings(site.settings:_*).
//  settings(site.jekyllSupport():_*).
//  settings(ghpages.settings:_*).
  settings(
    update / aggregate := false,
    publishArtifact := false,
    publishTo := Some(Resolver.file("Not actually used but required by publish-signed", file("/tmp/bogusrepo"))),
//    siteMappings ++= siteMappings.in(timberApi).value.map { case (f,t) => (f,t.replace("latest/api","docs/timber-api")) },
//    siteMappings ++= siteMappings.in(timberBackend).value.map { case (f,t) => (f,t.replace("latest/api","docs/timber-backend")) },
//    siteMappings ++= (sourceDirectory.in(JekyllSupport.Jekyll).value ** "*.svg") pair relativeTo(sourceDirectory.in(JekyllSupport.Jekyll).value),
    git.remoteRepo := "https://github.com/scalawag/timber.git"
  )
  .aggregate(
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
