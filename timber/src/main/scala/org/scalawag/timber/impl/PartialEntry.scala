package org.scalawag.timber.impl

import org.scalawag.timber.api._
import org.scalawag.timber.api.impl.Entry

case class PartialEntry(message: Option[Message] = None,
                        logger: Option[String] = None,
                        level: Option[Level] = None,
                        timestamp: Option[Long] = None,
                        thread: Option[Thread] = None,
                        tags: Option[Set[Tag]] = None,
                        context: Option[Map[String, List[String]]] = None)

object PartialEntry {
  def apply(entry:Entry):PartialEntry =
    PartialEntry(message = Some(entry.message),
                 logger = Some(entry.logger),
                 level = Some(entry.level),
                 timestamp = Some(entry.timestamp),
                 thread = Some(entry.thread),
                 tags = Some(entry.tags),
                 context = Some(entry.context))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
