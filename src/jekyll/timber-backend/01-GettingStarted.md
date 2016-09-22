---
layout: page
title: Getting Started
permalink: timber-backend/GettingStarted
group: "timber-backend"
---

To handle log entries created with any logging API using timber, you need to use the timber backend.

* Add the following dependency (and resolver, if necessary) to your sbt build:

  ~~~~
  resolvers += "sonatype-oss-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

  libraryDependencies += "org.scalawag.timber" %% "timber-backend" % "{{site.version}}"
  ~~~~
  {: .language-scala}

* Create a logger and log away!

  ~~~~
  import org.scalawag.timber.api._

  object Main {
    def main(args:Array[String]): Unit = {
      val log = new Logger
      log.trace("trace message")
    }
  }
  ~~~~
  {: .language-scala}


## A More Complex Example

Most dispatcher configurations won't be as simple as the examples above.  Here's an example of a more realistic (and
more complex) configuration just to give you a taste.  For more detail, you should read the section on the timber
[DSL](DSL).

~~~~
import org.scalawag.timber.api.Level._
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

val config = Configuration {
  fanout (
    ( level >= WARN ),
    ( loggingClass startsWith "com.example" ) ~> ( level >= DEBUG )
  ) ~> file("application.log")
}
~~~~
{: .language-scala}

This will send any entries that have a level of `WARN` or higher _or_ were logged from a class in the `com.example`
package and have a level of `DEBUG` or higher to the file `application.log`.  Any other entries will be dropped.

## Next steps

* Change the [default dispatcher](Dispatchers#default_dispatcher).
* Change the [format](Formatters) of logged entries.
* Learn more about [dispatchers](Dispatchers).
* Use a more complex dispatcher [configuration](DSL).
* [Integrate](Receivers#logrotate) with [logrotate](https://github.com/logrotate/logrotate).
