package org.scalawag.timber.api

import org.scalawag.timber.api.impl.{EntryDispatcher, Entry}

/** The main application interface into timber.
  *
  * Loggers are what applications use to inject @link Entries into the system.
  *
  * Loggers don't support the isEnabled type methods provided by some other logging systems.  That's because
  * the Logger doesn't make any decisions regarding where (or if) the entry it creates is actually written into
  * a log file or processed in any way.
  *
  * The idea behind timber is that the application thread gets to return to what it's doing as quickly as possible.
  *
  * Arguably, this is better for the application code because your application code shouldn't be dependent on the
  * logging configuration.  You have to try really hard to write code that depends on logging configuration.
  *
  * @param name the value that this Logger uses as the "logger" field in the Entries that it generates.
  */

class Logger(val name:String,val dispatcher:EntryDispatcher) {

  /** Submits an entry to the logging system.
    *
    * @param level the level to use for the entry created
    * @param message the message to include with the entry
    * @param tags the (optional) set of tags to include with the entry
    */

  def log(level:Level,message:Message,tags:Set[Tag]) {
    dispatcher.dispatch(buildEntry(level,message,tags))
  }

  def log(level:Level,message:Message):Unit = log(level,message,Set.empty[Tag])
  def log(level:Level,message:Message,tag:Tag*):Unit = log(level,message,tag.toSet)
  def log(level:Level,tag:Tag*)(message:Message):Unit = log(level,message,tag.toSet)

  protected def buildEntry(level:Level,message:Message,tags:Set[Tag]) =
    Entry(message = message,
          logger = this.name,
          level = level,
          timestamp = System.currentTimeMillis,
          thread = Thread.currentThread,
          tags = tags,
          context = LoggingContext.get)

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
