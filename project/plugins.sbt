// timber -- Copyright 2012 Justin Patterson -- All Rights Reserved

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.7.0")

resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("org.scalawag.sbt.gitflow" %% "sbt-gitflow" % "2.0.0-SNAPSHOT")