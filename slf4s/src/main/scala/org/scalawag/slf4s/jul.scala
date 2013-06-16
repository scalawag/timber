package org.scalawag.slf4s

import org.scalawag.slf4s

package object jul {

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

  trait Finest extends slf4s.Logger {
    val FINEST = Logging.Level.FINEST

    abstract override protected def getLevelName = ({ case FINEST => "FINEST" }:LevelNamer) orElse super.getLevelName

    def finest(message:Message):Unit = log(FINEST,message,Set.empty[Tag])
    def finest(message:Message,tag:Tag*):Unit = log(FINEST,message,tag.toSet)
    def finest(tag:Tag*)(message:Message):Unit = log(FINEST,message,tag.toSet)
  }

  trait Finer extends slf4s.Logger {
    val FINER = Logging.Level.FINER

    abstract override protected def getLevelName = ({ case FINER => "FINER" }:LevelNamer) orElse super.getLevelName

    def finer(message:Message):Unit = log(FINER,message,Set.empty[Tag])
    def finer(message:Message,tag:Tag*):Unit = log(FINER,message,tag.toSet)
    def finer(tag:Tag*)(message:Message):Unit = log(FINER,message,tag.toSet)
  }

  trait Fine extends slf4s.Logger {
    val FINE = Logging.Level.FINE

    abstract override protected def getLevelName = ({ case FINE => "FINE" }:LevelNamer) orElse super.getLevelName

    def fine(message:Message):Unit = log(FINE,message,Set.empty[Tag])
    def fine(message:Message,tag:Tag*):Unit = log(FINE,message,tag.toSet)
    def fine(tag:Tag*)(message:Message):Unit = log(FINE,message,tag.toSet)
  }

  trait Config extends slf4s.Logger {
    val CONFIG = Logging.Level.CONFIG

    abstract override protected def getLevelName = ({ case CONFIG => "CONFIG" }:LevelNamer) orElse super.getLevelName

    def config(message:Message):Unit = log(CONFIG,message,Set.empty[Tag])
    def config(message:Message,tag:Tag*):Unit = log(CONFIG,message,tag.toSet)
    def config(tag:Tag*)(message:Message):Unit = log(CONFIG,message,tag.toSet)
  }

  trait Info extends slf4s.Logger {
    val INFO = Logging.Level.INFO

    abstract override protected def getLevelName = ({ case INFO => "INFO" }:LevelNamer) orElse super.getLevelName

    def info(message:Message):Unit = log(INFO,message,Set.empty[Tag])
    def info(message:Message,tag:Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:Tag*)(message:Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warning extends slf4s.Logger {
    val WARNING = Logging.Level.WARNING

    abstract override protected def getLevelName = ({ case WARNING => "WARNING" }:LevelNamer) orElse super.getLevelName

    def warning(message:Message):Unit = log(WARNING,message,Set.empty[Tag])
    def warning(message:Message,tag:Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:Tag*)(message:Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Severe extends slf4s.Logger {
    val SEVERE = Logging.Level.SEVERE

    abstract override protected def getLevelName = ({ case SEVERE => "SEVERE" }:LevelNamer) orElse super.getLevelName

    def severe(message:Message):Unit = log(SEVERE,message,Set.empty[Tag])
    def severe(message:Message,tag:Tag*):Unit = log(SEVERE,message,tag.toSet)
    def severe(tag:Tag*)(message:Message):Unit = log(SEVERE,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = slf4s.Logger with Finest with Finer with Fine with Config with Info with Warning with Severe
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
