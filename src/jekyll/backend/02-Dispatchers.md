---
layout: page
title: Dispatchers
permalink: timber-backend/Dispatchers
group: "timber-backend"
---

Dispatchers route [entries](Entries) to the appropriate [receivers](Receivers) based on their [configuration](DSL).
Each [logger](../timber-api/Loggers) must be associated with exactly one dispatcher when its constructed. After that,
a logger's dispatcher can not be change. The specified dispatcher will handle all the entries generated by that logger
for the logger's lifetime. While a given logger uses only one dispatcher, any number of loggers may use the same
dispatcher.

Dispatchers are represented by the trait `org.scalawag.timber.api.Dispatcher` in the timber API. You can implement
this trait yourself, but there shouldn't be a reason for most applications.  You can simply use the concrete
dispatcher (`org.scalawag.timber.backend.dispatcher.Dispatcher`) supplied by the timber backend.
The rest of this page deals solely with the backend's concrete `Dispatcher` class unless otherwise noted.

If the dispatcher is not specified at logger construction, either through an explicit parameter or an implicit
dispatcher in scope, the _default dispatcher_ will be used.

<a name="default_dispatcher"></a>

## The Default Dispatcher

The _default dispatcher_ handles entries from loggers that don't specify an another one.  It also handles entries
bridged from other logging systems, unless otherwise configured. In the simple example in
[GettingStarted](GettingStarted), no dispatcher is specified when the logger is instantiated, so the default
dispatcher is used.  There is always a default dispatcher in place when using the timber backend.

The _initial_ default dispatcher (the one that's in place when your application starts) writes all its entries
to `stderr` with a default format. If you want timber to do something with the entries other than write them all
to `stderr`, you'll need to configure the default dispatcher.

Here's an example that limits the entries written to only those with a level of INFO or higher.

~~~~
import org.scalawag.timber.api.Logger
import org.scalawag.timber.api.Level.INFO
import org.scalawag.timber.backend.DefaultDispatcher
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

object Main {
  def setupLogging(): Unit = {
    val config = Configuration {
      ( level >= INFO ) ~> stderr
    }
    val disp = new Dispatcher(config)
    DefaultDispatcher.set(disp)
  }

  def main(args:Array[String]): Unit = {
    setupLogging()

    val log = new Logger
    log.trace("trace message")
    log.error("error message")
  }
}

Main.main(Array.empty)
~~~~
{: .language-scala}

You can't configure the initial default dispatcher per se.  However, you can create a new dispatcher and put
_it_ in place as the default dispatcher.  You will then have a reference to that dispatcher so that you can
reconfigure it. That's exactly what's happening in the example above: `disp` is created and configured and then
established as the new default dispatcher with the call to `DefaultDispatcher.set()`.  From that point on, any logger
that hasn't specified a dispatcher will use `disp`.  In the example, `disp` (the reference) is not used after that.
In your application, you could store the reference so that you could subsequently change the configuration.

Where you want to establish your default dispatcher is up to you.  It should probably be in your application startup
code for the most consistent logging experience.  It's not critical that it happen before logging calls begin but
you may get some entry leakage on stderr if you don't have it configured prior to those calls being made.

You can change the default dispatcher at any time during the life of your application.  Just call
`DefaultDispatcher.set()` again. All the entries will either go to the old one or the new one. None will be lost.

## Configuring a Dispatcher

You tell a dispatcher how to route entries by configuring it with a routing graph.  The easiest way to build the
graph is through the timber [DSL](DSL). This can either be done through a constructor parameter when the dispatcher
is created or by calling the `setConfiguration(Configuration)` or  `configure(fn)` method.  How you choose to provide
the configuration is largely a matter of style.

All three of the following dispatchers have effectively the same configuration.

~~~~
import org.scalawag.timber.api.Level.INFO
import org.scalawag.timber.backend.dispatcher._
import org.scalawag.timber.backend.dispatcher.configuration._
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

val cfg:Configuration = ( level >= INFO ) ~> stderr

val disp1 = new Dispatcher(cfg)

val disp2 = new Dispatcher
disp2.setConfiguration(cfg)

val disp3 = new Dispatcher
disp3.configure { IN =>
  IN ~> ( level >= INFO ) ~> stderr
}
~~~~
{: .language-scala}

For more information on building the Configuration itself, see [[DSL]].

## Multiple Dispatchers

Normally, there will be only one dispatcher in use in your application, though there's no reason that you can't
use multiple if the need arises. The most likely case is when you have multiple streams of entries that are always
generated from different loggers and always written to different destinations.  An example is the request log on a
web server.  Request logs don't really have levels (everything is the same level) and you don't normally want
the request log entries mixed in with your application debug logging.  Also, there's generally one location in the
code that generates all of the entries for the request log, as opposed to application debugging which is probably
all over the place.

Instead of trying to use a specific attribute or tag to identify the request log entries, it probably makes sense to
just keep the two streams separate.

~~~~
import org.scalawag.timber.api._
import org.scalawag.timber.api.Level._
import org.scalawag.timber.backend.DefaultDispatcher
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.buffering.ImmediateFlushing
import org.scalawag.timber.backend.receiver.formatter.MessageOnlyEntryFormatter

object Main {
  val requestLogDispatcher = new Dispatcher(Configuration(
    file("request.log",ImmediateFlushing)(MessageOnlyEntryFormatter)
  ))

  val applicationLogDispatcher = new Dispatcher(Configuration(
    ( level >= INFO ) ~> file("application.log",ImmediateFlushing)
  ))

  def main(args:Array[String]): Unit = {
    // Create a logger for the request log, specifying the dispatcher.
    val requestLog = new BaseLogger()(requestLogDispatcher)

    // Use the other dispatcher as the default dispatcher.
    DefaultDispatcher.set(applicationLogDispatcher)

    val log = new Logger
    log.trace("trace message")
    requestLog.log("handled request from 127.0.0.1 in 39 ms")
    log.error("error message")
  }
}

Main.main(Array.empty)
~~~~
{: .language-scala}

## Configuration Caching

By default, dispatchers evaluate the configuration every time an entry is dispatched. This can be rather expensive if
you have a complex graph. To improve performance, you can tell the dispatcher to cache constrained configurations
for similar entries based on a subset of their attributes by providing the second argument (a `CacheKeyExtractor`)
to the `Dispatcher` constructor.

A key extractor supplies a single method `extractKey(Entry)` which extracts distinguishing fields from the entry
and returns them in an `EntryFacets`. Depending on your configuration and the distribution of entries you expect
to generate, this can significantly improve the performance of your dispatcher.  Key fields should be significant
in the configuration graph or else you'll waste memory caching the same configuration for multiple partial entries.

For example, given the (ridiculously bad) configuration graph:

~~~~
choose (
  when ( level >= ERROR ) ~> ( tagged MyTag ) ~> r1,
  when ( level >= WARN )  ~> ( tagged MyTag ) ~> r2,
  when ( level >= INFO )  ~> ( tagged MyTag ) ~> r3,
  otherwise ~> r4
)
~~~~
{: .language-scala}

Sure, it's easy to see how to make this configuration more efficient but it's just to illustrate configuration
caching.  In this case, you'd want to create a cache key extractor that makes the entry's tags the significant key.
That's because the graph is wildly different depending on whether or not `MyTag` is present on the entry.

Doing that will cause the dispatcher to cache two constrained graphs: one for entries whose tag set is
`Set(MyTag)` and one whose tag set is `Set()` (I'm assuming that there aren't any other tags being used in
the logging system or else this might not be as effective).  Now, instead of evaluating the entire graph for each
entry, the dispatcher will first use `extractKey` to get a `EntryFacets` for each `Entry`.  In the fake world
we've contrived, that will produce one of the following partial entries, which will cache the corresponding
constrained graph.

 - `EntryFacets(tags = Some(Set(MyTag)))`

   ~~~~
   choose (
     when ( level >= ERROR ) ~> r1,
     when ( level >= WARN )  ~> r2,
     when ( level >= INFO )  ~> r3,
     otherwise ~> r4
   )
   ~~~~
   {: .language-scala}


 - `EntryFacets(tags = Some(Set()))`

   ~~~~
   r4
   ~~~~
   {: .language-scala}


As you can see, the graph for the former becomes somewhat simpler but the graph for the latter becomes
incredibly simple, no longer containing any branches.  If we know that most of the entries in our application
will not have the `MyTag` tag, this is a big win for the performance of our dispatcher.

### CacheKeyExtractor factory

To make it easier to create cache key extractors, timber comes with a factory to help you generate the more common
ones.  You just need to specify the entry attributes that you want to key off of and it will generate the extractor.
Here's an example:

~~~~
import org.scalawag.timber.backend.dispatcher.EntryDispatcher._

val extractor = CacheKeyExtractor(Attribute.Tags)
~~~~
{: .language-scala}

This code generates the cache key extractor from the previous section, where only the `tags` are significant for
caching.  If you want to use a more complex key, you can specify multiple attributes.

~~~~
import org.scalawag.timber.backend.dispatcher.EntryDispatcher._

val extractor = CacheKeyExtractor(Attribute.CallingClass,Attribute.Level)
~~~~
{: .language-scala}

This would tell the dispatcher that it should cache constrained configurations for calling class name and level.
This would make sense if those are the only two attributes that your configuration uses to determine where entries
are received. If there are too many calling classes, though, this might use up too much memory.

### Custom CacheKeyExtractor

Suppose that you have many Tags flowing around in your system but you still have the call distribution from above
(mostly entries with empty `tags`).  You may not want to use the `CacheKeyExtractor` factory method because it will
cache the configuration for every combination of tags present on any entry.  In this scenario, you can create a
custom cache key extractor that only distinguishes between empty `tags` and non-empty `tags`.

~~~~
import org.scalawag.timber.api.Tag
import org.scalawag.timber.api.impl.Entry
import org.scalawag.timber.backend.dispatcher.PartialEntry
import org.scalawag.timber.backend.dispatcher.EntryDispatcher.CacheKeyExtractor

val extractor = new CacheKeyExtractor {
  object DummyTag extends Tag

  def extractKey(entry:Entry): PartialEntry = {
    if ( entry.tags.isEmpty )
      PartialEntry(tags = Some(Set()))
    else
      PartialEntry(tags = Some(Set(DummyTag)))
  }
}
~~~~
{: .language-scala}


## Changing the initial DefaultDispatcher

If you're in a situation where you can't wait until your bootstrap code to execute before changing the default
dispatcher, you have another option available to you.  Setting the system property `timber.dispatcher.class` will
cause timber to use that class to create the initial default dispatcher.  The specified class must have a default
constructor or you will get an error and `org.scalawag.timber.backend.dispatcher.Dispatcher`, the default initial
default dispatcher (sorry, I couldn't resist), will be used instead.

This dispatcher can still be replaced at runtime through the mechanism shown above.  This just makes it so that you
can establish your preferred dispatcher earlier.

## Next Steps

That's really all you need to know about the dispatchers. Of course, you haven't really learned much about creating
their configurations with the [DSL](DSL) yet and that's the most interesting thing about dispatchers.