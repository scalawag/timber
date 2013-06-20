package org.scalawag.timber.api.style

import org.scalawag.timber.api
import org.scalawag.timber.api.impl.{DefaultEntryDispatcherLoader, EntryDispatcher}

package object jul {

  object Level {
    import api.{Level => std}
    val FINEST  = std.TRACE - 100 as "FINEST"
    val FINER   = std.TRACE as "FINER"
    val FINE    = std.DEBUG as "FINE"
    val CONFIG  = std.INFO as "CONFIG"
    val INFO    = std.INFO + 100
    val WARNING = std.WARN as "WARNING"
    val SEVERE  = std.ERROR as "SEVERE"
  }

  import Level._

  trait Finest extends api.Logger {
    def finest(message:api.Message):Unit = log(FINEST,message,Set.empty[api.Tag])
    def finest(message:api.Message,tag:api.Tag*):Unit = log(FINEST,message,tag.toSet)
    def finest(tag:api.Tag*)(message:api.Message):Unit = log(FINEST,message,tag.toSet)
  }

  trait Finer extends api.Logger {
    def finer(message:api.Message):Unit = log(FINER,message,Set.empty[api.Tag])
    def finer(message:api.Message,tag:api.Tag*):Unit = log(FINER,message,tag.toSet)
    def finer(tag:api.Tag*)(message:api.Message):Unit = log(FINER,message,tag.toSet)
  }

  trait Fine extends api.Logger {
    def fine(message:api.Message):Unit = log(FINE,message,Set.empty[api.Tag])
    def fine(message:api.Message,tag:api.Tag*):Unit = log(FINE,message,tag.toSet)
    def fine(tag:api.Tag*)(message:api.Message):Unit = log(FINE,message,tag.toSet)
  }

  trait Config extends api.Logger {
    def config(message:api.Message):Unit = log(CONFIG,message,Set.empty[api.Tag])
    def config(message:api.Message,tag:api.Tag*):Unit = log(CONFIG,message,tag.toSet)
    def config(tag:api.Tag*)(message:api.Message):Unit = log(CONFIG,message,tag.toSet)
  }

  trait Info extends api.Logger {
    def info(message:api.Message):Unit = log(INFO,message,Set.empty[api.Tag])
    def info(message:api.Message,tag:api.Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:api.Tag*)(message:api.Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warning extends api.Logger {
    def warning(message:api.Message):Unit = log(WARNING,message,Set.empty[api.Tag])
    def warning(message:api.Message,tag:api.Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:api.Tag*)(message:api.Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Severe extends api.Logger {
    def severe(message:api.Message):Unit = log(SEVERE,message,Set.empty[api.Tag])
    def severe(message:api.Message,tag:api.Tag*):Unit = log(SEVERE,message,tag.toSet)
    def severe(tag:api.Tag*)(message:api.Message):Unit = log(SEVERE,message,tag.toSet)
  }

  class Logger(override val name:String,override val dispatcher:EntryDispatcher)
    extends api.Logger(name,dispatcher) with Finest with Finer with Fine with Config with Info with Warning with Severe

  class LoggerFactory(val dispatcher:EntryDispatcher = DefaultEntryDispatcherLoader.dispatcher) extends api.LoggerFactory[Logger] {
    def getLogger(name:String) = new Logger(name,dispatcher)
  }

  object LoggerFactory extends LoggerFactory(DefaultEntryDispatcherLoader.dispatcher)

  trait Logging extends api.Logging[Logger] {
    protected[this] val loggerFactory = LoggerFactory
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
