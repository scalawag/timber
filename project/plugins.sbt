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

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")

//addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.6")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

//addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.1")

//resolvers += "jgit-repo" at "https://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

resolvers += Resolver.sonatypeRepo("snapshots")

//addSbtPlugin("org.scalawag.sbt.gitflow" %% "sbt-gitflow" % "2.0.0-SNAPSHOT")
