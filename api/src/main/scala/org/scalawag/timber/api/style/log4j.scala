package org.scalawag.timber.api.style

import org.scalawag.timber.api
import org.scalawag.timber.api.impl.{DefaultEntryDispatcherLoader, EntryDispatcher}

package object log4j {

  object Level {
    val TRACE = api.Level.TRACE
    val DEBUG = api.Level.DEBUG
    val INFO  = api.Level.INFO
    val WARN  = api.Level.WARN
    val ERROR = api.Level.ERROR
    val FATAL = api.Level.ERROR + 100 as "FATAL"
  }

  import Level._

  trait Trace extends api.Logger {
    def trace(message:api.Message):Unit = log(TRACE,message,Set.empty[api.Tag])
    def trace(message:api.Message,tag:api.Tag*):Unit = log(TRACE,message,tag.toSet)
    def trace(tag:api.Tag*)(message:api.Message):Unit = log(TRACE,message,tag.toSet)
  }
  
  trait Debug extends api.Logger {
    def debug(message:api.Message):Unit = log(DEBUG,message,Set.empty[api.Tag])
    def debug(message:api.Message,tag:api.Tag*):Unit = log(DEBUG,message,tag.toSet)
    def debug(tag:api.Tag*)(message:api.Message):Unit = log(DEBUG,message,tag.toSet)
  }
  
  trait Info extends api.Logger {
    def info(message:api.Message):Unit = log(INFO,message,Set.empty[api.Tag])
    def info(message:api.Message,tag:api.Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:api.Tag*)(message:api.Message):Unit = log(INFO,message,tag.toSet)
  }
  
  trait Warn extends api.Logger {
    def warn(message:api.Message):Unit = log(WARN,message,Set.empty[api.Tag])
    def warn(message:api.Message,tag:api.Tag*):Unit = log(WARN,message,tag.toSet)
    def warn(tag:api.Tag*)(message:api.Message):Unit = log(WARN,message,tag.toSet)
  }
  
  trait Error extends api.Logger {
    def error(message:api.Message):Unit = log(ERROR,message,Set.empty[api.Tag])
    def error(message:api.Message,tag:api.Tag*):Unit = log(ERROR,message,tag.toSet)
    def error(tag:api.Tag*)(message:api.Message):Unit = log(ERROR,message,tag.toSet)
  }

  trait Fatal extends api.Logger {
    def fatal(message:api.Message):Unit = log(FATAL,message,Set.empty[api.Tag])
    def fatal(message:api.Message,tag:api.Tag*):Unit = log(FATAL,message,tag.toSet)
    def fatal(tag:api.Tag*)(message:api.Message):Unit = log(FATAL,message,tag.toSet)
  }

  class Logger(override val name:String,override val dispatcher:EntryDispatcher)
    extends api.Logger(name,dispatcher) with Trace with Debug with Info with Warn with Error with Fatal

  class LoggerFactory(val dispatcher:EntryDispatcher = DefaultEntryDispatcherLoader.dispatcher) extends api.LoggerFactory[Logger] {
    def getLogger(name:String) = new Logger(name,dispatcher)
  }

  object LoggerFactory extends LoggerFactory(DefaultEntryDispatcherLoader.dispatcher)

  trait Logging extends api.Logging[Logger] {
    protected[this] val loggerFactory = LoggerFactory
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
