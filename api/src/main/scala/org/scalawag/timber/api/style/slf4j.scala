package org.scalawag.timber.api.style

import org.scalawag.timber.api
import org.scalawag.timber.api.impl.{DefaultEntryDispatcherLoader, EntryDispatcher}

package object slf4j {

  object Level {
    val TRACE = log4j.Level.TRACE
    val DEBUG = log4j.Level.DEBUG
    val INFO  = log4j.Level.INFO
    val WARN  = log4j.Level.WARN
    val ERROR = log4j.Level.ERROR
  }

  trait Trace extends log4j.Trace
  trait Debug extends log4j.Debug
  trait Info extends log4j.Info
  trait Warn extends log4j.Warn
  trait Error extends log4j.Error

  class Logger(override val name:String,override val dispatcher:EntryDispatcher)
    extends api.Logger(name,dispatcher) with Trace with Debug with Info with Warn with Error

  class LoggerFactory(val dispatcher:EntryDispatcher = DefaultEntryDispatcherLoader.dispatcher) extends api.LoggerFactory[Logger] {
    def getLogger(name:String) = new Logger(name,dispatcher)
  }

  object LoggerFactory extends LoggerFactory(DefaultEntryDispatcherLoader.dispatcher)

  trait Logging extends api.Logging[Logger] {
    protected[this] val loggerFactory = LoggerFactory
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
