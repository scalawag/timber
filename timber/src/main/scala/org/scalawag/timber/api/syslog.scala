package org.scalawag.timber.api

import org.scalawag.timber.api
import org.scalawag.timber.impl.dispatcher.{Management, SynchronousEntryDispatcher}
import org.scalawag.timber.impl.LoggerImpl

package object syslog {
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
      val DEBUG     = 10
      val INFO      = 20
      val NOTICE    = 30
      val WARNING   = 40
      val ERROR     = 50
      val CRITICAL  = 60
      val ALERT     = 70
      val EMERGENCY = 80
    }
  }

  trait Debug extends api.Logger {
    import Logging.Level.DEBUG

    abstract override protected def getLevelName = ({ case DEBUG => "DEBUG" }:LevelNamer) orElse super.getLevelName

    def debug(message:Message):Unit = log(DEBUG,message,Set.empty[Tag])
    def debug(message:Message,tag:Tag*):Unit = log(DEBUG,message,tag.toSet)
    def debug(tag:Tag*)(message:Message):Unit = log(DEBUG,message,tag.toSet)
  }

  trait Info extends api.Logger {
    import Logging.Level.INFO

    abstract override protected def getLevelName = ({ case INFO => "INFO" }:LevelNamer) orElse super.getLevelName

    def info(message:Message):Unit = log(INFO,message,Set.empty[Tag])
    def info(message:Message,tag:Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:Tag*)(message:Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Notice extends api.Logger {
    import Logging.Level.NOTICE

    abstract override protected def getLevelName = ({ case NOTICE => "NOTICE" }:LevelNamer) orElse super.getLevelName

    def notice(message:Message):Unit = log(NOTICE,message,Set.empty[Tag])
    def notice(message:Message,tag:Tag*):Unit = log(NOTICE,message,tag.toSet)
    def notice(tag:Tag*)(message:Message):Unit = log(NOTICE,message,tag.toSet)
  }

  trait Warning extends api.Logger {
    import Logging.Level.WARNING

    abstract override protected def getLevelName = ({ case WARNING => "WARNING" }:LevelNamer) orElse super.getLevelName

    def warning(message:Message):Unit = log(WARNING,message,Set.empty[Tag])
    def warning(message:Message,tag:Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:Tag*)(message:Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Error extends api.Logger {
    import Logging.Level.ERROR

    abstract override protected def getLevelName = ({ case ERROR => "ERROR" }:LevelNamer) orElse super.getLevelName

    def error(message:Message):Unit = log(ERROR,message,Set.empty[Tag])
    def error(message:Message,tag:Tag*):Unit = log(ERROR,message,tag.toSet)
    def error(tag:Tag*)(message:Message):Unit = log(ERROR,message,tag.toSet)
  }

  trait Critical extends api.Logger {
    import Logging.Level.CRITICAL

    abstract override protected def getLevelName = ({ case CRITICAL => "CRITICAL" }:LevelNamer) orElse super.getLevelName

    def critical(message:Message):Unit = log(CRITICAL,message,Set.empty[Tag])
    def critical(message:Message,tag:Tag*):Unit = log(CRITICAL,message,tag.toSet)
    def critical(tag:Tag*)(message:Message):Unit = log(CRITICAL,message,tag.toSet)
  }

  trait Alert extends api.Logger {
    import Logging.Level.ALERT

    abstract override protected def getLevelName = ({ case ALERT => "ALERT" }:LevelNamer) orElse super.getLevelName

    def alert(message:Message):Unit = log(ALERT,message,Set.empty[Tag])
    def alert(message:Message,tag:Tag*):Unit = log(ALERT,message,tag.toSet)
    def alert(tag:Tag*)(message:Message):Unit = log(ALERT,message,tag.toSet)
  }

  trait Emergency extends api.Logger {
    import Logging.Level.EMERGENCY

    abstract override protected def getLevelName = ({ case EMERGENCY => "EMERGENCY" }:LevelNamer) orElse super.getLevelName

    def emergency(message:Message):Unit = log(EMERGENCY,message,Set.empty[Tag])
    def emergency(message:Message,tag:Tag*):Unit = log(EMERGENCY,message,tag.toSet)
    def emergency(tag:Tag*)(message:Message):Unit = log(EMERGENCY,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = api.Logger with Emergency with Alert with Critical with Error with Warning with Notice with Info with Debug

  // This is the class that defines a LoggerFactory/EntryDispatcher that deals with the Logger type defined above.
  // It's defined here as a class in case someone wants multiple LoggerManagers that share the same style of Logging
  // (the same Logger type) without using the same configuration.  In most cases, clients will just use the singleton
  // below.

  class LoggerManager extends SynchronousEntryDispatcher[Logger] with LoggerFactory[Logger] with Management {
    override def getLogger(name:String):Logger =
      // Here's where any functional mixins should be included.  Note that it can return differently functional
      // Loggers (through mixins or different base classes) depending on the name (or anything else).  As far as the
      // caller is concerned, though, the interface on the resulting logger should be the same.  It would probably
      // be considered bad form to downcast your logger and use special abilities for them.
      new LoggerImpl(name,this) with Emergency with Alert with Critical with Error with Warning with Notice with Info with Debug
  }

  // This is the singleton defined with the configuration in this file.  It can be used when this style of Logger
  // is desired and only a single configuration is needed for all clients.

  object LoggerManager extends LoggerManager
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
