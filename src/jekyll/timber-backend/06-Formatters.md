---
layout: page
title: Formatters
permalink: timber-backend/Formatters
group: "timber-backend"
---

Formatters take [entries](Entries) and turn them into text that can be written to a file or stream or transferred to
another system.  Formatters control all aspects of the formatting, including what metadata headers are included and
how dates and times are represented.

Most [receivers](Receivers) that eventually turn their entries into text take a formatter as a constructor parameter.
It can usually either be supplied explicitly or implicitly.

## ProgrammableEntryFormatter

Timber comes with a flexible entry formatter that you can probably use for most cases called the
`ProgrammableEntryFormatter`.  It comes with its own mini-DSL to determine what metadata you want to appear in
with your entries.  The default entry formatter used by timber dispatchers if you don't specify another one is
actually just a configuration of a `ProgrammableEntryFormatter`.  Here's an example of creating one.

~~~~
import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter
import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter._
import org.scalawag.timber.backend.receiver.formatter.timestamp._
import org.scalawag.timber.backend.receiver.formatter.level._

object MyEntryFormatter extends ProgrammableEntryFormatter(Seq(
  entry.timestamp formattedWith HumanReadableTimestampFormatter,
  entry.level formattedWith NameLevelFormatter,
  entry.loggingClass,
  entry.threadName,
  entry.sourceLocation,
  entry.tags formattedWith Commas,
  entry.threadAttributes map TopsOnly formattedWith CommasAndEquals
))
~~~~
{: .language-scala}

### Metadata DSL

The constructor for `ProgrammableEntryFormatter` allows you to specify a list of metadata providers that you want it
to use to extract metadata from its entries.  These providers can be access through fields on the `entry` object as
seen in the example above.

These are the providers that are built-in to the `ProgrammableEntryFormatter`.

* `threadName` -- the name of the thread that created the entry
* `timestamp` -- the timestamp at which the entry was created
* `level` -- the level at which the entry was created (if available)
* `loggingClass` -- the class from which the log method was called (if available)
* `loggingMethod` -- the method from which the log method was called (if available)
* `sourceLocation` -- the location in source (file name and line number) from which the log method was called (if available)
* `tags` -- the tags associated with the entry
* `loggerAttributes` -- the logger attributes associated with the entry
* `threadAttributes` -- the thread attributes associated with the entry
* `loggerAttribute(`_`name`_`)` -- the logger attribute with the specified name associated with the entry (if available)
* `threadAttribute(`_`name`_`)` -- the thread attribute with the specified name associated with the entry (if available)

~~~~
new ProgrammableEntryFormatter(Seq(
  entry.threadName,
  entry.loggingClass
))
~~~~
{: .language-scala}

You can also specify Strings to be included literally.

~~~~
new ProgrammableEntryFormatter(Seq(
  "literal",
  entry.loggingClass
))
~~~~
{: .language-scala}

In addition to the providers above, you can specify some modifiers to tweak them before their inclusion:

* `formattedWith` -- allows you to choose how the metadata is formatted (and must match the type of the metadata)
* `map` -- allows you to apply an arbitrary mapping function to the metadata before inclusion
* `without` -- allows you to remove keys from a map before inclusion (e.g., if you already included on of the
keys specifically)

If you don't specify a formatter, the `toString` method of the object will be used. There are some built-in
formatters for [timestamps](org.scalawag.timber.backend.receiver.formatter.timestamp) and
[levels](org.scalawag.timber.backend.receiver.formatter.level) that you can use.  There are also some built-in
formatters for any `Iterable`s that may be more appealing than the default `toString` implementation.

* `Commas` - formats Iterables as strings with commas separating the items
* `Spaces` - formats Iterables as strings with spaces separating the items
* `Delimiter(`_`str`_`)` - formats `Iterable`s as strings with the specified delimiter (`str`) separating the items
* `CommasAndEquals` - formats Maps as equals-separated pairs separated by commas (e.g., `k1=v1,k2=v2`)

~~~~
entry.timestamp formattedWith HumanReadableTimestampFormatter
entry.timestamp formattedWith SimpleDateFormatTimestampFormatter("yyyy/MM/dd")
entry.threadName map { name => name.length }
entry.loggerAttributes without "name" formattedWith CommasAndEquals
~~~~
{: .language-scala}

For the metadata that may not be present (indicated with "if available" above), you can specify a fallback to use when it's absent with `orElse`.
You can't use `orElse` after a metadata provider that will definitely return something.

~~~~
entry.loggingClass orElse entry.loggerAttribute("name") orElse "unknown"
~~~~
{: .language-scala}

With certain combinations of modifiers, you may have to include parentheses if the scala compiler can't figure out
the operator precedence.  You can also specify the modifiers without utilizing the infix call style, if you prefer.

## Change the Formatter for the Default Dispatcher

The entry format is tied into the [dispatcher](Dispatcher) configuration through a [receiver](Receivers).  If you
want to change the format of the default dispatcher, you'll need to establish a new default dispatcher so that you
have a reference to it and can change its configuration. Here's an example very similar to the one in
[Getting Started](GettingStarted) except that this changes the formatter used by the default dispatcher to include
only the timestamp as metadata with the logged message.

~~~~
import org.scalawag.timber.api.Logger
import org.scalawag.timber.api.Level.INFO
import org.scalawag.timber.backend.DefaultDispatcher
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter
import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter._

object Main {
  def setupLogging(): Unit = {
    // Implicitly specify an EntryFormatter
    implicit val formatter = new ProgrammableEntryFormatter(Seq(entry.timestamp))
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
