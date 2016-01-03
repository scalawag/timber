---
layout: page
title: Receivers
permalink: backend/Receivers
group: "timber-backend"
---

Receivers do something interesting with [entries](Entries).  What they do is entirely up to them and dependent on
their code and configuration.  They are the final destination for entries.  Indeed, they provide the reason that you
bother to create entries in the first place.

Normally, receivers are used to write Entries to a file or terminal but they can do anything you please:

* send them as email messages,
* send them to an alerting system, or
* forward them to a central logging server.

## Built-in Stackable Receivers

Currently, timber comes with few built-in receivers but they handle most of the normal use cases.  Anything they
don't handle can be done with a custom receiver.  The built-in receivers are _stackable_.  This means that
there some composable behaviors (detailed in the following sections) that timber providers which you can mix into
the receivers when you create them.

|`ConsoleOutReceiver`          |writes to `Console.out` which is usually `stdout` but can be redirected
|`ConsoleErrReceiver`          |writes to `Console.err` which is usually `stderr` but can be redirected
|`WriterBasedStackableReceiver`|writes to a `Writer` that you provide a creation function for at construction

### Concurrency Policies

By default, receivers in timber have no concurrency control.  Any thread can call the receiver methods and all the
calls are handled concurrently.  If you want to impose some control, which is usually a good idea if you're
application is multithreaded, you should use one of the following traits.

|`NoThreadSafety`|provides no concurrency control (the default)
|`Locking`       |provides concurrency control through locking (better for minimizing thread count)
|`Queueing`      |provides concurrency control through worker queues (better for message throughput)

### Buffering Policies

By default, receivers in timber never flush their resources.  They rely on the underlying resources themselves to
flush when necessary.  This provides the best performance, as it minimizes I/O calls.  However, sometimes, you'll
want to flush more frequently.  For example, suppose that you have a process watching a log file for errors so that
it can send an alert.  You want the error to be flushed immediately.  You don't want to wait until enough errors have
amassed that the file's buffer is full.  By then, it's too late.

If you want to change the default buffering behavior, mix in one of the following policies.

|`LazyFlushing`     |never flushes (the default, better for throughput, worse for latency)
|`ImmediateFlushing`|flushes every time an entry is written (better for latency, worse for throughput)
|`PeriodicFlushing` |flushed at least as often as its specified period (defaults to 5 seconds)

It's important to realize that this is just flushing that's initiated from the receiver.  Flushing can occur in the
underlying resource and it will have no effect on the receiver's policy (for example, a flush in the underlying
resource won't reset the `PeriodicFlushing` timer).

### Examples

~~~~
import java.io.FileWriter
import org.scalawag.timber.backend.receiver._
import org.scalawag.timber.backend.receiver.buffering._
import org.scalawag.timber.backend.receiver.concurrency._
import org.scalawag.timber.backend.receiver.formatter._

val ra = new WriterBasedStackableReceiver(new FileWriter("/tmp/a")) with PeriodicFlushing with Locking
val rb = new ConsoleOutReceiver(DefaultEntryFormatter) with ImmediateFlushing with Queueing
val rc = new ConsoleErrReceiver(DefaultEntryFormatter)
~~~~
{: .language-scala}

### Using the Configuration DSL

The configuration DSL provides some convenient methods for creating file-based (and filehandle-based) receivers.
Since you aren't using constructors, you can't mix in the policy traits.  The DSL functions still allow you to take
advantage of them as parameters, though.

~~~~
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.buffering._
import org.scalawag.timber.backend.receiver.concurrency._
import scala.concurrent.duration._

val ra = file("/tmp/a",PeriodicFlushing,Queueing)
val rb = file("/tmp/b",PeriodicFlushing(1.minute),Locking)
val rc = file("/tmp/c",ImmediateFlushing,NoThreadSafety)
val rd = file("/tmp/d",LazyFlushing,Queueing)
~~~~
{: .language-scala}

## Custom Receivers

If you need a receiver that does something other than what the timber built-ins receivers do, you'll need to create
a custom receiver.  You must implement the `org.scalawag.timber.backend.receiver.Receiver` trait.  If you want to
take advantage of timber's policy stacking, you have two options.  You can either extend
`org.scalawag.timber.backend.receiver.StackableReceiver` and then your users (including you) can use the policy
mixins at construction or you can implement `Receiver` directly and inform your users (including you) to use the
`StackableReceiver` constructor to allow the mixins.

## Ensuring Receiver Closure

Timber doesn't automatically close receivers (or their underlying resources).  This is because you can change the
configuration at any point and it doesn't make sense to close and reopen all of the resources unless they're
actually being decommissioned.  You could also change the dispatcher configuration so that it's no longer dispatching
to a specific receiver but another dispatcher is still using that receiver.  This is messy enough that timber washes
its hands of managing the receivers automatically.  This means that you need to close your own receivers when you're
done using them.  This is especially important if you're using a aggressive buffering behavior.  Anything not
flushed will be lost when the process shuts down.

To make life a little easier for you, timber _does_ provide a way to give it some responsibility.  Passing your
receiver to `Receiver.closeOnShutdown()` means that timber will install a shutdown hook to close your receiver
during a normal system shutdown.  This isn't foolproof.  There are situations (mostly crashes) where the JVM will
exit without calling the shutdown hooks and your entries could be lost.  If this isn't OK with you, you should do
something else -- maybe change the buffering policy on your receiver to `ImmediateFlushing`.  The shutdown hook
strategy should cover most normal cases, though.

<a name="logrotate"></a>

## Integration with logrotate

If you want to integrate with an external service like [logrotate](https://github.com/logrotate/logrotate), you'll
need a way for that service to inform your receivers when they need to close and reopen their file handles.
Otherwise, your receiver will keep its existing filehandle and continue writing to the old file even if it has been
moved or deleted.  One of the preferred ways to provide this notification is through
[UNIX signals](https://en.wikipedia.org/wiki/Unix_signal).

Similar to the way timber provide close-on-shutdown, it also provides the ability to easily wire your receivers up
to a signal handler so that your receivers can close and reopen when the signal is received.  This example ties the
receiver to the `SIGHUP` signal (a common choice).

~~~~
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver._

val r = file("application.log")
Receiver.closeOnSignal("HUP",r)
~~~~
{: .language-scala}
