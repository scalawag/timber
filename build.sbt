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

val commonSettings = Seq(
  organization := "org.scalawag.timber",
  scalaVersion := "2.12.13",
  crossScalaVersions := Seq("2.12.13", "2.13.5"),
  exportJars := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
//  testOptions += Tests.Argument("-oDF"),
  publishMavenStyle := true,
  homepage := Some(url("http://scalawag.org/timber")),
  startYear := Some(2012),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.8",
    "org.scalamock" %% "scalamock" % "5.1.0"
  ) map (_ % "test")
)

val timberApi = project
  .in(file("timber-api"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-api",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )

val timberBackend = project
  .in(file("timber-backend"))
  .dependsOn(timberApi)
  .settings(commonSettings: _*)
  .settings(
    name := "timber-backend"
  )

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
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
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
  publish / skip := true
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

val TimberApi = config("timberApi")
val TimberBackend = config("timberBackend")

val timber = project
  .in(file("."))
  .enablePlugins(JekyllPlugin)
  .settings(
    update / aggregate := false,
    publishArtifact := false,
    publishTo := Some(Resolver.file("Not actually used but required by publish-signed", file("/tmp/bogusrepo"))),
    SiteScaladocPlugin.scaladocSettings(TimberApi, timberApi / Compile / packageDoc / mappings, "docs/timber-api"),
    SiteScaladocPlugin
      .scaladocSettings(TimberBackend, timberBackend / Compile / packageDoc / mappings, "docs/timber-backend"),
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
