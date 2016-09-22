---
layout: page
title: Messages
permalink: timber-api/Messages
group: "timber-api"
---

Messages provide the text content for entries in the logging system. This is one of the areas where timber attempts
to improve upon the classic JVM application logging interfaces (starting with log4j) by taking advantage of
Scala's language features.

Internally, each message is an object that produces the text (as a `String`) lazily on demand.  The work of creating
the message isn't performed until it's needed, either because the filtering rules require it or because it's being
sent to a log file or bridged to another logging system.  As a user of the timber API, you will hopefully remain
blissfully unaware of the existence of the `Message` class. You normally won't create one explicitly but will use
one of the implicit conversions in the `Message` companion object.  This is now known as the
[Magnet Pattern](http://spray.io/blog/2012-12-13-the-magnet-pattern/).

Because of the lazy nature of messages, timber loggers don't support the `isEnabled()` style methods common to most
JVM logging systems.  That's because you don't need to guard against unnecessary and expensive string-building code.
Timber will compute the string value exactly once the first time that it's required.

## String

The simplest case is where an expression returning a `String` is used as the message.  The string is converted by-name,
which means that even though it may look like you're wasting cycles generating a string when it's not being logged,
you're not.  Scala makes it so that no work is done unless it's required.

~~~~
// A simple String literal
log.debug("my simple message")

// A simple String expression
val simple = true
log.debug("my " + ( if ( simple ) "simple" else "complex" ) + " message")

// A thunk that returns a String
log.debug {
  val adjective =
    if ( simple )
      "simple"
    else
      "complex"
  "my " + adjective + " message"
}

// An interpolated string
log.debug(s"my ${ if ( simple ) "simple" else "complex" } message")
~~~~
{: .language-scala}

These calls all produce the same message and they're all lazy.

## Throwable

Timber takes a different approach to `Throwable` stack traces than most other logging systems.  There is no special
handling for throwables in log entries.  As far as timber is concerned, stack traces are just another source of text
for an entry and that's all.  If all the text that you want to log is the stack trace of a throwable, you can just use
the throwable directly as your message and it will be converted into a message containing its stack trace.

~~~~
try {
  throw new Exception("boom!")
} catch {
  case ex: Exception => log.debug(ex)
}
~~~~
{: .language-scala}

Of course, the work of producing the stack trace won't be done unless it turns out that the message text is needed.

## (String,Throwable)

With some other logging systems, like log4j, you can include a message along with your throwable's stack trace by
including both a message _and_ a throwable in the logging call.  With timber, there is a tuple conversion that allows
you to do the same thing.

~~~~
try {
  throw new Exception("boom!")
} catch {
  case ex: Exception => log.debug("I caught an exception!",ex)
}
~~~~
{: .language-scala}

The resulting message will contain the line "I caught an exception!" followed by the output of `ex.printStackTrace`
on the lines that follow.

## PrintWriter => Unit

Sometimes, it may be easier for you to use a `PrintWriter` to add multiple lines of text to the log entry. Even if
it's not really easier, it's a good idea because it will keep the lines of the entry together. Timber always treats
an entry as an indivisible unit, even if it has multiple lines.  This means that it won't be split across multiple
files (for rolling configurations) or broken up by another thread's entry.

This can be broken up because it generates eleven entries.

~~~~
val nums = Stream.from(0).take(10)

pw.println("Some kind of heading:")
nums foreach { n =>
  log.debug(n.toString)
}
~~~~
{: .language-scala}

This is better style (timber-wise) because it produces one multi-line entry.

~~~~
val nums = Stream.from(0).take(10)

log.debug { pw:PrintWriter =>
  pw.println("Some kind of heading:")
  nums foreach pw.println
}
~~~~
{: .language-scala}


Note that you have to explicitly type the argument to your function literal as a `PrintWriter` or else timber (more
specifically, Scala) won't understand what you're trying to do.

It may not be worth repeating but I'll repeat it anyway -- none of this work happens (including the creation of the
`PrintWriter`) unless the message text proves to be necessary.

## ImmediateMessage

After all this talk of lazy message calculation, it's important to point out that there are some situations where this
is undesirable behavior. If you have entries with messages that are built out of `var`s whose values may change by
the time the entry eventually makes it to its destination, you probably want to capture their value at the time of
logging. You can indicate this to a logger by adding the `ImmediateMessage` tag to the entry. (This is the only
tag that has any significance to timber.)  It tells the logger to calculate the message text synchronously during the
logging call.

~~~~
var name = "alice"
log.debug(Set(ImmediateMessage))(s"sending to $name")
name = "bob"
~~~~
{: .language-scala}

If you add the `ImmediateMessage` tag to a _logger_, it makes it so that all of that logger's entry's messages are
calculated immediately.  There may be situations where this is warranted.

## Next Steps

* Learn about timber's [levels](Levels).
