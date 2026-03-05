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

import com.jsuereth.sbtpgp.SbtPgp
import com.jsuereth.sbtpgp.SbtPgp.autoImport._
import sbt.Keys._
import sbt.{Def, _}
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport._
import xerial.sbt.Sonatype.sonatypeCentralHost

object ScalawagPublishing extends AutoPlugin {
  override def requires = plugins.JvmPlugin && SbtPgp && Sonatype
  override def trigger = allRequirements

  private def travisFail(msg: String): Option[String] =
    sys.env.get("TRAVIS") match {
      case Some("true") => throw new Exception(msg)
      case _            => None
    }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      Test / publishArtifact := false,
      sonatypeProfileName := "org.scalawag",
      sonatypeCredentialHost := sonatypeCentralHost,
      ThisBuild / organizationName := "Scalawag",
      ThisBuild / organizationHomepage := Some(url("http://scalawag.org/")),
      ThisBuild / publishTo := {
        if (isSnapshot.value)
          Some("central-snapshots" at "https://central.sonatype.com/repository/maven-snapshots/")
        else
          localStaging.value
      },
      ThisBuild / homepage := Some(url(s"https://github.com/scalawag/${name.value}")),
      ThisBuild / startYear := Some(2012),
      ThisBuild / licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
      ThisBuild / scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/scalawag/${name.value}"),
          s"scm:git:git://github.com/scalawag/${name.value}.git"
        )
      ),
      ThisBuild / developers := List(
        Developer("justinp", "Justin Patterson", "justin@scalawag.org", url("https://github.com/justinp"))
      ),
      useGpg := false,
      usePgpKeyHex("439444E02ED9335F91C538455283F6A358FB8629"),
      pgpPublicRing := (ThisBuild / baseDirectory).value / "project" / "public.gpg",
      pgpSecretRing := (ThisBuild / baseDirectory).value / "project" / "private.gpg",
      pgpPassphrase := sys.env.get("PGP_PASSPHRASE").orElse(travisFail("missing $PGP_PASSPHRASE")).map(_.toArray),
      credentials ++= {
        for {
          user <- sys.env.get("SONATYPE_USER").orElse(travisFail("missing $SONATYPE_USER"))
          password <- sys.env.get("SONATYPE_PASSWORD").orElse(travisFail("missing $SONATYPE_PASSWORD"))
        } yield Credentials("Sonatype Nexus Repository Manager", "central.sonatype.com", user, password)
      }
    )

}
