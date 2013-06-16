package org.scalawag.slf4s


/** The main application interface into timber.
  *
  * Loggers are what applications use to inject @link Entrys into the system.
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
  * Usage patterns:
  *
  * Simple string message:
  *
  * log.log(1,"blah")
  *
  * Multiline message:
  */

trait Logger {

  /** The value that this Logger uses as the "logger" field in the Entries that it generates. */

  val name:String

  protected[this] type LevelNamer = PartialFunction[Int,String]

  /** Provides a string for each level supported by this Logger.  The default implementation in this
    * trait simply returns the numeric value as a string.  Traits that add logical levels (through the addition
    * of level-specific methods) can abstractly override this method to provide their own strings for some levels
    * and defer to this method for everything else.
    */

  protected[this] def getLevelName:LevelNamer = { case n => n.toString }

  /** Submits an entry to the logging system.
    *
    * @param level the level to use for the entry created
    * @param message the message to include with the entry
    * @param tags the (optional) set of tags to include with the entry
    */

  def log(level:Int,message:Message,tags:Set[Tag])

  def log(level:Int,message:Message):Unit = log(level,message,Set.empty[Tag])
  def log(level:Int,message:Message,tag:Tag*):Unit = log(level,message,tag.toSet)
  def log(level:Int,tag:Tag*)(message:Message):Unit = log(level,message,tag.toSet)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
