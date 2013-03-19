package org.scalawag.timber.api

import org.scalawag.timber.api
import org.scalawag.timber.impl.dispatcher.{Management, SynchronousEntryDispatcher}
import org.scalawag.timber.impl.LoggerImpl

package object jul {
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
      val FINEST  = 10
      val FINER   = 20
      val FINE    = 30
      val CONFIG  = 40
      val INFO    = 50
      val WARNING = 60
      val SEVERE  = 70
    }
  }

  trait Finest extends api.Logger {
    val FINEST = Logging.Level.FINEST

    abstract override protected def getLevelName = ({ case FINEST => "FINEST" }:LevelNamer) orElse super.getLevelName

    def finest(message:Message):Unit = log(FINEST,message,Set.empty[Tag])
    def finest(message:Message,tag:Tag*):Unit = log(FINEST,message,tag.toSet)
    def finest(tag:Tag*)(message:Message):Unit = log(FINEST,message,tag.toSet)
  }

  trait Finer extends api.Logger {
    val FINER = Logging.Level.FINER

    abstract override protected def getLevelName = ({ case FINER => "FINER" }:LevelNamer) orElse super.getLevelName

    def finer(message:Message):Unit = log(FINER,message,Set.empty[Tag])
    def finer(message:Message,tag:Tag*):Unit = log(FINER,message,tag.toSet)
    def finer(tag:Tag*)(message:Message):Unit = log(FINER,message,tag.toSet)
  }

  trait Fine extends api.Logger {
    val FINE = Logging.Level.FINE

    abstract override protected def getLevelName = ({ case FINE => "FINE" }:LevelNamer) orElse super.getLevelName

    def fine(message:Message):Unit = log(FINE,message,Set.empty[Tag])
    def fine(message:Message,tag:Tag*):Unit = log(FINE,message,tag.toSet)
    def fine(tag:Tag*)(message:Message):Unit = log(FINE,message,tag.toSet)
  }

  trait Config extends api.Logger {
    val CONFIG = Logging.Level.CONFIG

    abstract override protected def getLevelName = ({ case CONFIG => "CONFIG" }:LevelNamer) orElse super.getLevelName

    def config(message:Message):Unit = log(CONFIG,message,Set.empty[Tag])
    def config(message:Message,tag:Tag*):Unit = log(CONFIG,message,tag.toSet)
    def config(tag:Tag*)(message:Message):Unit = log(CONFIG,message,tag.toSet)
  }

  trait Info extends api.Logger {
    val INFO = Logging.Level.INFO

    abstract override protected def getLevelName = ({ case INFO => "INFO" }:LevelNamer) orElse super.getLevelName

    def info(message:Message):Unit = log(INFO,message,Set.empty[Tag])
    def info(message:Message,tag:Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:Tag*)(message:Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warning extends api.Logger {
    val WARNING = Logging.Level.WARNING

    abstract override protected def getLevelName = ({ case WARNING => "WARNING" }:LevelNamer) orElse super.getLevelName

    def warning(message:Message):Unit = log(WARNING,message,Set.empty[Tag])
    def warning(message:Message,tag:Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:Tag*)(message:Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Severe extends api.Logger {
    val SEVERE = Logging.Level.SEVERE

    abstract override protected def getLevelName = ({ case SEVERE => "SEVERE" }:LevelNamer) orElse super.getLevelName

    def severe(message:Message):Unit = log(SEVERE,message,Set.empty[Tag])
    def severe(message:Message,tag:Tag*):Unit = log(SEVERE,message,tag.toSet)
    def severe(tag:Tag*)(message:Message):Unit = log(SEVERE,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = api.Logger with Finest with Finer with Fine with Config with Info with Warning with Severe

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
      new LoggerImpl(name,this) with Finest with Finer with Fine with Config with Info with Warning with Severe
  }

  // This is the singleton defined with the configuration in this file.  It can be used when this style of Logger
  // is desired and only a single configuration is needed for all clients.

  object LoggerManager extends LoggerManager
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
