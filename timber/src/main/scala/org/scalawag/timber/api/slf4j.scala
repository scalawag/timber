package org.scalawag.timber.api

import org.scalawag.timber.api
import org.scalawag.timber.impl.dispatcher.{Management, AsynchronousEntryDispatcher}
import org.scalawag.timber.impl.LoggerImpl

package object slf4j {
  // This is the trait that you should extend if you want to use the singleton defined below for your logging.

  trait Logging extends api.Logging[Logger] {
    protected[this] val loggerFactory = LoggerManager
  }

  // This is a list of the numerical equivalent of the levels that can be used in this style.  They're defined here
  // as a handy way for the configuration to refer to the levels.  Unfortunately, there's no way for this to be made
  // available automatically based on the Logger type defined, so for the greatest amount of sense-making, you just
  // need not to refer to the levels in this list that your loggers don't actually handle.

  object Logging {
    object Level {
      val TRACE = 10
      val DEBUG = 20
      val INFO  = 30
      val WARN  = 40
      val ERROR = 50
      val FATAL = 60
    }
  }

  trait Trace extends api.Logger {
    val TRACE = Logging.Level.TRACE

    abstract override protected def getLevelName = ({ case TRACE => "TRACE" }:LevelNamer) orElse super.getLevelName

    def trace(message:Message):Unit = log(TRACE,message,Set.empty[Tag])
    def trace(message:Message,tag:Tag*):Unit = log(TRACE,message,tag.toSet)
    def trace(tag:Tag*)(message:Message):Unit = log(TRACE,message,tag.toSet)
  }

  trait Debug extends api.Logger {
    val DEBUG = Logging.Level.DEBUG

    abstract override protected def getLevelName = ({ case DEBUG => "DEBUG" }:LevelNamer) orElse super.getLevelName

    def debug(message:Message):Unit = log(DEBUG,message,Set.empty[Tag])
    def debug(message:Message,tag:Tag*):Unit = log(DEBUG,message,tag.toSet)
    def debug(tag:Tag*)(message:Message):Unit = log(DEBUG,message,tag.toSet)
  }

  trait Info extends api.Logger {
    val INFO = Logging.Level.INFO

    abstract override protected def getLevelName = ({ case INFO => "INFO" }:LevelNamer) orElse super.getLevelName

    def info(message:Message):Unit = log(INFO,message,Set.empty[Tag])
    def info(message:Message,tag:Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:Tag*)(message:Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warn extends api.Logger {
    val WARN = Logging.Level.WARN

    abstract override protected def getLevelName = ({ case WARN => "WARN" }:LevelNamer) orElse super.getLevelName

    def warn(message:Message):Unit = log(WARN,message,Set.empty[Tag])
    def warn(message:Message,tag:Tag*):Unit = log(WARN,message,tag.toSet)
    def warn(tag:Tag*)(message:Message):Unit = log(WARN,message,tag.toSet)
  }

  trait Error extends api.Logger {
    val ERROR = Logging.Level.ERROR

    abstract override protected def getLevelName = ({ case ERROR => "ERROR" }:LevelNamer) orElse super.getLevelName

    def error(message:Message):Unit = log(ERROR,message,Set.empty[Tag])
    def error(message:Message,tag:Tag*):Unit = log(ERROR,message,tag.toSet)
    def error(tag:Tag*)(message:Message):Unit = log(ERROR,message,tag.toSet)
  }

  trait Fatal extends api.Logger {
    val FATAL = Logging.Level.FATAL

    abstract override protected def getLevelName = ({ case FATAL => "FATAL" }:LevelNamer) orElse super.getLevelName

    def fatal(message:Message):Unit = log(FATAL,message,Set.empty[Tag])
    def fatal(message:Message,tag:Tag*):Unit = log(FATAL,message,tag.toSet)
    def fatal(tag:Tag*)(message:Message):Unit = log(FATAL,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = api.Logger with Trace with Debug with Info with Warn with Error

  // This is the class that defines a LoggerFactory/EntryDispatcher that deals with the Logger type defined above.
  // It's defined here as a class in case someone wants multiple LoggerManagers that share the same style of Logging
  // (the same Logger type) without using the same configuration.  In most cases, clients will just use the singleton
  // below.

  class LoggerManager extends AsynchronousEntryDispatcher[Logger] with LoggerFactory[Logger] with Management {
    override def getLogger(name:String):Logger =
      // Here's where any functional mixins should be included.  Note that it can return differently functional
      // Loggers (through mixins or different base classes) depending on the name (or anything else).  As far as the
      // caller is concerned, though, the interface on the resulting logger should be the same.  It would probably
      // be considered bad form to downcast your logger and use special abilities for them.
      new LoggerImpl(name,this) with Trace with Debug with Info with Warn with Error
  }

  // This is the singleton defined with the configuration in this file.  It can be used when this style of Logger
  // is desired and only a single configuration is needed for all clients.

  object LoggerManager extends LoggerManager
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
