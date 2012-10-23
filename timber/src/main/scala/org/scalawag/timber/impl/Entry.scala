package org.scalawag.timber.impl

import scala.collection.immutable.Stack
import org.scalawag.timber.api.{Tag, Message}

object Entry {
  case class Location(filename:String,lineNumber:Int) {
    override def toString = "%s:%d".format(filename,lineNumber)
  }
}

case class Entry(message:Message,
                 logger: String,
                 level: Int,
                 levelName: String,
                 timestamp: Long = System.currentTimeMillis,
                 thread: Thread = Thread.currentThread,
                 tags: Set[Tag] = Set(),
                 context: Map[String, Stack[String]] = Map(),
                 location: Option[Entry.Location] = None)

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
