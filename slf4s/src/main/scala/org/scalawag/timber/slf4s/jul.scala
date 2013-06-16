package org.scalawag.timber.slf4s

import org.scalawag.timber.slf4s
import org.scalawag.timber.slf4s.impl.EntryDispatcher

package object jul {

  // This is a list of the numerical equivalent of the levels that can be used in this style.  They're defined here
  // as a handy way for the configuration to refer to the levels.  Unfortunately, there's no way for this to be made
  // available automatically based on the Logger type defined, so for the greatest amount of sense-making, you just
  // need not to refer to the levels in this list that your loggers don't actually handle.

  object Logging {
    object Level {
      import slf4s.{Level => std}
      val FINEST  = std.FINEST
      val FINER   = std.FINER
      val FINE    = std.FINE
      val CONFIG  = std.INFO as "CONFIG"
      val INFO    = std.INFO + 100
      val WARNING = std.WARNING
      val SEVERE  = std.ERROR as "SEVERE"
    }
  }

  trait Finest extends slf4s.Logger {
    import Logging.Level.FINEST

    def finest(message:Message):Unit = log(FINEST,message,Set.empty[Tag])
    def finest(message:Message,tag:Tag*):Unit = log(FINEST,message,tag.toSet)
    def finest(tag:Tag*)(message:Message):Unit = log(FINEST,message,tag.toSet)
  }

  trait Finer extends slf4s.Logger {
    import Logging.Level.FINER

    def finer(message:Message):Unit = log(FINER,message,Set.empty[Tag])
    def finer(message:Message,tag:Tag*):Unit = log(FINER,message,tag.toSet)
    def finer(tag:Tag*)(message:Message):Unit = log(FINER,message,tag.toSet)
  }

  trait Fine extends slf4s.Logger {
    import Logging.Level.FINE

    def fine(message:Message):Unit = log(FINE,message,Set.empty[Tag])
    def fine(message:Message,tag:Tag*):Unit = log(FINE,message,tag.toSet)
    def fine(tag:Tag*)(message:Message):Unit = log(FINE,message,tag.toSet)
  }

  trait Config extends slf4s.Logger {
    import Logging.Level.CONFIG

    def config(message:Message):Unit = log(CONFIG,message,Set.empty[Tag])
    def config(message:Message,tag:Tag*):Unit = log(CONFIG,message,tag.toSet)
    def config(tag:Tag*)(message:Message):Unit = log(CONFIG,message,tag.toSet)
  }

  trait Info extends slf4s.Logger {
    import Logging.Level.INFO

    def info(message:Message):Unit = log(INFO,message,Set.empty[Tag])
    def info(message:Message,tag:Tag*):Unit = log(INFO,message,tag.toSet)
    def info(tag:Tag*)(message:Message):Unit = log(INFO,message,tag.toSet)
  }

  trait Warning extends slf4s.Logger {
    import Logging.Level.WARNING

    def warning(message:Message):Unit = log(WARNING,message,Set.empty[Tag])
    def warning(message:Message,tag:Tag*):Unit = log(WARNING,message,tag.toSet)
    def warning(tag:Tag*)(message:Message):Unit = log(WARNING,message,tag.toSet)
  }

  trait Severe extends slf4s.Logger {
    import Logging.Level.SEVERE

    def severe(message:Message):Unit = log(SEVERE,message,Set.empty[Tag])
    def severe(message:Message,tag:Tag*):Unit = log(SEVERE,message,tag.toSet)
    def severe(tag:Tag*)(message:Message):Unit = log(SEVERE,message,tag.toSet)
  }

  // This type includes all of the interface mixins that we want our Logging clients to have available to them.
  // Functional mixins don't need to be included here.

  type Logger = slf4s.Logger with Finest with Finer with Fine with Config with Info with Warning with Severe

  trait LoggerFactory extends slf4s.LoggerFactory[Logger] {
    protected val dispatcher:EntryDispatcher

    def getLogger(name:String):Logger =
      new slf4s.Logger(name,dispatcher) with Finest with Finer with Fine with Config with Info with Warning with Severe
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
