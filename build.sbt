// timber -- Copyright 2012-2026 -- Justin Patterson
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

ThisBuild / versionScheme := Some("early-semver")

val commonSettings = Seq(
  organization := "org.scalawag.timber",
  scalaVersion := "2.12.19",
  crossScalaVersions := Seq("2.12.19", "2.13.17", "3.3.4"),
  exportJars := true,
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-language:implicitConversions",
        "-Wconf:cat=deprecation&since<2.12&origin=scala.*:e",
        "-Wconf:cat=deprecation&since>2.11&origin=scala.*:s",
      )
      case Some((2, _)) => Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-language:implicitConversions",
      )
      case Some((3, _)) => Seq(
        "-feature",
        "-language:implicitConversions",
      )
      case _ => Seq("-feature")
    }
  },
  publishMavenStyle := true,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.19",
    "org.scalamock" %% "scalamock" % "7.4.0"
  ) map (_ % "test")
)

val timberApi = project
  .in(file("timber-api"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-api",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.4.2"
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
    libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.3"
  ) dependsOn (timberApi)

val timberOverSlf4j = project
  .in(file("timber-over-slf4j"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-over-slf4j",
    libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.3",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.3" % Test
  ) dependsOn (timberApi)

val logbackSupport = project
  .in(file("timber-logback-support"))
  .settings(commonSettings: _*)
  .settings(
    name := "timber-logback-support",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.3"
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
    publish / skip := true,
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
