package org.scalawag.timber.impl

import org.scalawag.timber.api.LoggingContext
import org.scalawag.timber.api.{Message, Tag, Logger}
import org.scalawag.timber.impl.dispatcher.EntryDispatcher

class LoggerImpl(val name: String,private val dispatcher:EntryDispatcher) extends Logger {

  protected def buildEntry(level:Int,message:Message,tags:Set[Tag]) =
    Entry(message = message,
          logger = this.name,
          level = level,
          levelName = getLevelName(level),
          timestamp = System.currentTimeMillis,
          thread = Thread.currentThread,
          tags = tags,
          context = LoggingContext.get)

  def log(level:Int,message:Message,tags:Set[Tag]) {
    dispatcher.dispatch(buildEntry(level,message,tags))
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
