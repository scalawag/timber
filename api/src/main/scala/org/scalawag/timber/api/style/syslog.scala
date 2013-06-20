package org.scalawag.timber.api.style

import org.scalawag.timber.api
import org.scalawag.timber.api.impl.{DefaultEntryDispatcherLoader, EntryDispatcher}

package object syslog {

  object Level {
    val DEBUG     = api.Level.DEBUG
    val INFO      = api.Level.INFO
    val NOTICE    = api.Level.INFO + 100  as "NOTICE"
    val WARNING   = api.Level.WARN as "WARNING"
    val ERROR     = api.Level.ERROR
    val CRITICAL  = api.Level.ERROR + 100 as "CRITICAL"
    val ALERT     = api.Level.ERROR + 200 as "ALERT"
    val EMERGENCY = api.Level.ERROR + 300 as "EMERGENCY"
  }

  import Level._

  trait Debug extends log4j.Debug

  trait Info extends log4j.Info

  trait Notice extends api.Logger {
    def notice(message:api.Message):Unit = log(NOTICE,message,Set.empty[api.Tag])
    def notice(message:api.Message,tag:api.Tag*):Unit = log(NOTICE,message,tag.toSet)
    def notice(tag:api.Tag*)(message:api.Message):Unit = log(NOTICE,message,tag.toSet)
  }

  trait Warning extends api.Logger {
    def warning(message:api.Message):Unit = log(WARNING,message,Set.empty[api.Tag])
    def warning(message:api.Message,tag:api.Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:api.Tag*)(message:api.Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Error extends log4j.Error

  trait Critical extends api.Logger {
    def critical(message:api.Message):Unit = log(CRITICAL,message,Set.empty[api.Tag])
    def critical(message:api.Message,tag:api.Tag*):Unit = log(CRITICAL,message,tag.toSet)
    def critical(tag:api.Tag*)(message:api.Message):Unit = log(CRITICAL,message,tag.toSet)
  }

  trait Alert extends api.Logger {
    def alert(message:api.Message):Unit = log(ALERT,message,Set.empty[api.Tag])
    def alert(message:api.Message,tag:api.Tag*):Unit = log(ALERT,message,tag.toSet)
    def alert(tag:api.Tag*)(message:api.Message):Unit = log(ALERT,message,tag.toSet)
  }

  trait Emergency extends api.Logger {
    def emergency(message:api.Message):Unit = log(EMERGENCY,message,Set.empty[api.Tag])
    def emergency(message:api.Message,tag:api.Tag*):Unit = log(EMERGENCY,message,tag.toSet)
    def emergency(tag:api.Tag*)(message:api.Message):Unit = log(EMERGENCY,message,tag.toSet)
  }

  class Logger(override val name:String,override val dispatcher:EntryDispatcher)
    extends api.Logger(name,dispatcher) with Emergency with Alert with Critical with Error with Warning with Notice with Info with Debug

  class LoggerFactory(val dispatcher:EntryDispatcher = DefaultEntryDispatcherLoader.dispatcher) extends api.LoggerFactory[Logger] {
    def getLogger(name:String) = new Logger(name,dispatcher)
  }

  object LoggerFactory extends LoggerFactory(DefaultEntryDispatcherLoader.dispatcher)

  trait Logging extends api.Logging[Logger] {
    protected[this] val loggerFactory = LoggerFactory
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
