---
layout: page
---

Timber is a logging system for Scala applications. It is written in Scala to take advantage of its unique language
features. It's not a wrapper for an existing Java logging library.

Configuration is done through an [internal DSL](timber-backend/ConfigurationDSL], allowing you to use the full 
power of Scala to configure your logging system.

~~~~
dispatcher.configure { IN =>
  IN ~> fanout (
    ( level >= DEBUG ),
    ( loggingClass startsWith "com.example" ) ~> ( level >= WARN )
  ) ~> stderr
}
~~~~
{: .language-scala}

It takes advantage of Scala's by-name parameters and function literals to provide lazy message evaluation. That
prevents doing work that doesn't need to be done without requiring "isEnabled" conditionals.

~~~~
log.debug(s"n = $n")

log.debug { pw: PrintWriter =>
  pw.print("Here's a multi-line log entry that")
  pw.println("won't be built unless it's needed.")
  traversable.foreach(pw.println)
}
~~~~
{: .language-scala}

It supports the [slf4j](http://www.slf4j.org/) interface so that it can be used by Java classes and by
applications that are already using the slf4j API (or any API for which slf4j includes a
[bridge](http://www.slf4j.org/legacy.html)).

It supports logback appenders so you can keep using any custom appenders that you've already written.

Get started by looking at the timber [concepts](Concepts).