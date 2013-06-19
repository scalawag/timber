package org.scalawag.timber.api

import org.scalawag.timber.api
import org.scalawag.timber.api.impl.{DefaultEntryDispatcherLoader, EntryDispatcher}

package object slf4j {

  // This is a list of the numerical equivalent of the levels that can be used in this style.  They're defined here
  // as a handy way for the configuration to refer to the levels.  Unfortunately, there's no way for this to be made
  // available automatically based on the Logger type defined, so for the greatest amount of sense-making, you just
  // need not to refer to the levels in this list that your loggers don't actually handle.

  object Logging {
    object Level {
      import api.{Level => std}
      val TRACE = std.FINE as "TRACE"
      val DEBUG = std.DEBUG
      val INFO  = std.INFO
      val WARN  = std.WARNING as "WARN"
      val ERROR = std.ERROR
      val FATAL = std.FATAL
    }
  }

  trait Trace extends api.Logger {
    import Logging.Level.TRACE

    def trace(message:api.Message):Unit = log(TRACE,message,Set.empty[api.Tag])
    def trace(message:api.Message,tag:api.Tag*):Unit = log(TRACE,message,tag.toSet)
    def trace(tag:api.Tag*)(message:api.Message):Unit = log(TRACE,message,tag.toSet)
  }

  trait Debug extends api.Logger {
    import Logging.Level.DEBUG

    def debug(message:api.Message):Unit = log(DEBUG,message,Set.empty[api.Tag])
    def debug(message:api.Message,tag:api.Tag*):Unit = log(DEBUG,message,tag.toSet)
    def debug(tag:api.Tag*)(message:api.Message):Unit = log(DEBUG,message,tag.toSet)
  }

  trait Info extends api.Logger {
    import Logging.Level.INFO

    def info(message:api.Message):Unit = log(INFO,message,Set.empty[api.Tag])
    def info(message:api.Message,tag:api.Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:api.Tag*)(message:api.Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warn extends api.Logger {
    import Logging.Level.WARN

    def warn(message:api.Message):Unit = log(WARN,message,Set.empty[api.Tag])
    def warn(message:api.Message,tag:api.Tag*):Unit = log(WARN,message,tag.toSet)
    def warn(tag:api.Tag*)(message:api.Message):Unit = log(WARN,message,tag.toSet)
  }

  trait Error extends api.Logger {
    import Logging.Level.ERROR

    def error(message:api.Message):Unit = log(ERROR,message,Set.empty[api.Tag])
    def error(message:api.Message,tag:api.Tag*):Unit = log(ERROR,message,tag.toSet)
    def error(tag:api.Tag*)(message:api.Message):Unit = log(ERROR,message,tag.toSet)
  }

  trait Fatal extends api.Logger {
    import Logging.Level.FATAL

    def fatal(message:api.Message):Unit = log(FATAL,message,Set.empty[api.Tag])
    def fatal(message:api.Message,tag:api.Tag*):Unit = log(FATAL,message,tag.toSet)
    def fatal(tag:api.Tag*)(message:api.Message):Unit = log(FATAL,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = api.Logger with Trace with Debug with Info with Warn with Error

  trait LoggerFactory extends api.LoggerFactory[Logger] {
    protected val dispatcher:EntryDispatcher

    def getLogger(name:String):Logger =
      new api.Logger(name,dispatcher) with Trace with Debug with Info with Warn with Error with Fatal
  }

  object LoggerFactory extends LoggerFactory {
    override protected val dispatcher:EntryDispatcher = DefaultEntryDispatcherLoader.dispatcher
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
