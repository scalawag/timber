---
layout: page
title: Thread Attributes
permalink: timber-api/ThreadAttributes
group: "timber-api"
---

Sometimes, you want to associate your entries with dynamic context, based on the thread's stack at the time the
logging call is made.  This is what timber's thread attributes are for.  Contrast this to the static context
provided by [logger attributes](Loggers#slc). Thread attributes are essentially maps of stacks of strings which
are automatically copied by a logger to its entries when it creates them.

The `ThreadAttributes` object provides an easy way to manage a thread's attributes.  As indicated before, each
attribute name maps to a stack of strings.  This is because, for dynamic context, you often want to set an attribute
for the duration of a block of code and then return it to its previous value when the block is complete.  This could
be to identify the subsystem within which the logging call takes place, for example.

For simple cases, use the `during()` method which both pushes a new value onto the stack and pops it once its
thunk is complete.

~~~~
ThreadAttributes.during("subsystem","model") {
  log.debug("msg1")
  ThreadAttributes.during("subsystem","db") {
    log.debug("msg2")
  }
  log.debug("msg3")
}
~~~~
{: .language-scala}

If you need to separate the push and pop (across method boundaries, for example), you can use the `push()` and `pop()`
methods.  The `pop()` method requires that you specify the values that you expect to be at the top of the stack to
help prevent errors in push/pop asymmetry.  Trying to pop the wrong values from the stacks will result in a runtime
exception being thrown.

~~~~
def enter(): Unit = {
  ThreadAttributes.push("subsystem","db")
}

def exit(): Unit = {
  ThreadAttributes.pop("subsystem","db")
}

log.debug("msg1")
enter()
log.debug("msg2")
exit()
log.debug("msg3")
~~~~
{: .language-scala}

Example use cases:

* Storing the client IP address of a web request so that it can be logged (using a thread-centric web library).
* Storing the role of an authenticated user on the thread so that admin actions can be logged at a different level.
