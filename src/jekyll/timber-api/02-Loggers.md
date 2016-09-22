---
layout: page
title: Loggers
permalink: timber-api/Loggers
group: "timber-api"
---

Loggers are objects used by timber API consumers to log entries.

Loggers in timber roughly correspond to loggers in other logging systems that you've probably used before. There are
a few differences that should be kept in mind, though.  Timber loggers are extremely lightweight, immutable and
thread-safe. There's almost no overhead in creating one, so there's no good reason to try to maintain a small set of
loggers or try hard to reuse them.  There's also no need to worry about concurrency control.  Feel free to use a
single logger for every thread and object in your library, if that's what is best for your situation.  Feel free to
create a new logger every time you need to log something if that's what works best.  Either extreme works fine for
timber.

Loggers are so lightweight because they are essentially just factories that know how to gather context and parameters
and put together an entry.  Then, they hand off the entry to a dispatcher for processing.  The dispatcher is a much
heavier and long-lived object.  It normally lives throughout the process' lifetime.  When a logger is created, it is
associated with a dispatcher which handles all of its entries.  While a logger can only have a single dispatcher, one
dispatcher can handle all the loggers in the system.

## Logger Attributes

Unlike other logging systems, timber loggers _don't_ have names. The names are usually used to represent the class
from which entries are logged.  This is an old pattern established by log4j many years ago.  In timber, this
information is gathered at compile-time through macros, so it's not tied to the logger.  You can use a single logger
and each entry will still know what class it was created by.

Loggers _can_ have arbitrary attributes, though.  So, if you _really_ want a logger name, you can just add a `name`
attribute to it.  Attributes are specified when the logger is created.

~~~~
import org.scalawag.timber.api._

val log = new Logger(Map("name" -> "com.example.MyClass"))
~~~~
{: .language-scala}

All the things that can be done with the logger name in other logging systems (e.g., filtering and inclusion in the
log files) can be done in timber as well (see [Conditions](../timber-backend/Conditions)).

## Logger Tags

Loggers may also have a set of [tags](Tags) associated with them.  These are just copied to every entry that the
logger creates.  Even though you can specify an entry's tags as parameters to the logging call, this gives you a
little more flexibility. You may, for example, decide to identify entries from a specific subsytem (or maybe all
the entries from your library) using a specific tag.  This makes it possible to do so without having to touch each
individual logging call.

Tags specified as parameters to the logging call are combined with the logger's tags to create the set of tags for
the entry.

<a name="slc"></a>

## Static Logging Context

The two collections mentioned above (logger attributes and logger tags) are imparted to every entry created by a
logger.  Combining this with the lightweight nature of timber loggers gives you the ability to use the logger to
maintain some static (or lexical) context for their entries (as opposed to the dynamic context provided by [thread
attributes](ThreadAttributes)).  This can be useful when you're using akka or scala Futures or some other technology
that renders threads completely fungible.

In this example, the client IP is associated with all of the entries in scope of `logger` even though it's possible
that the logging method calls are being executed by three different threads.

~~~~
import scala.util._
import scala.concurrent.Future
import org.scalawag.timber.api._
import scala.concurrent.ExecutionContext.Implicits.global

def handleRequest(clientIp:String) {
  val logger = new Logger(Map("clientIp" -> clientIp))
  logger.debug("about to start handling request")
  Future {
    logger.debug("handling request")
  } onComplete {
    case Success(_) =>
      logger.debug("request handled")
    case Failure(ex) =>
      logger.debug(s"failed to handle request: $$ex")
  }
}
~~~~
{: .language-scala}

## Logging Methods

In addition to the generic `log()` method available on timbers `BaseLogger`, loggers may also provide level-specific
logging methods.  If you don't like the particular set of methods supported by a logger, you can roll your own,
using the built in level mixins.

~~~~
import org.scalawag.timber.api._
import org.scalawag.timber.api.level._

val log = new BaseLogger with Emergency with Fatal with Finest
~~~~
{: .language-scala}

You can also create your own custom log levels.  See [Advanced Usage](AdvancedUsage) for more information.

## Where's `isEnabled`?

BaseLoggers don't support the `isEnabled()` type methods provided by some other JVM logging systems.  That's because
the logger doesn't make any decisions regarding if (or where) the entries it creates are actually processed in any way.
You don't need to protect against message generation overhead because [messages](Messages) are lazily built
(unless you specify the `ImmediateMessage` tag). Anything else that you would do in application code that
depends on the status of the logging system is probably a bad idea.

## Next Steps

* Learn more about entry [messages](Messages).
* Learn about timber's [levels](Levels).
