package org.scalawag.timber.impl

import org.scalawag.timber.api.{Tag, Message}
import collection.immutable.Stack

case class PartialEntry(message: Option[Message] = None,
                        logger: Option[String] = None,
                        level: Option[Int] = None,
                        throwable: Option[Option[Throwable]] = None,
                        timestamp: Option[Long] = None,
                        thread: Option[Thread] = None,
                        tags: Option[Set[Tag]] = None,
                        context: Option[Map[String, Stack[String]]] = None)

object PartialEntry {
  def apply(entry:Entry):PartialEntry =
    PartialEntry(message = Some(entry.message),
                 logger = Some(entry.logger),
                 level = Some(entry.level),
                 throwable = Some(entry.throwable),
                 timestamp = Some(entry.timestamp),
                 thread = Some(entry.thread),
                 tags = Some(entry.tags),
                 context = Some(entry.context))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
