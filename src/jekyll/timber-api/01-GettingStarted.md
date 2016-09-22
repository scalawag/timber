---
layout: page
title: Getting Started
permalink: timber-api/GettingStarted
group: "timber-api"
---

To generate log entries for any logging backend using timber, you need to use the timber API.

* Add the following dependency (and resolver, if necessary) to your sbt build:

  ~~~~
  resolvers += "sonatype-oss-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

  libraryDependencies += "org.scalawag.timber" %% "timber-api" % "{{site.version}}"
  ~~~~
  {: .language-scala}

* Create a logger and log away!

  ~~~~
  import org.scalawag.timber.api._

  class MyLibraryClass {
    private[this] val log = new Logger

    def doSomething(): Unit = {
      log.trace("trace message")
    }
  }
  ~~~~
  {: .language-scala}

## Next Steps

* Find out more about [loggers](Loggers).
* Learn about timber's [levels](Levels).
* Find out how to specify more complicated [messages](Messages).
* Explore [best practices](BestPractices).
