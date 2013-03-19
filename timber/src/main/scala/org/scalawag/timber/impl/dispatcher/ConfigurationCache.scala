package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.Entry
import org.scalawag.timber.impl.{InternalLogging, PartialEntry}
import java.util.concurrent.atomic.AtomicReference

trait ConfigurationCache extends EntryDispatcher with Configurable with InternalLogging {
  private val cache = new AtomicReference(Map[PartialEntry,Configuration]())

  def extractKey(entry:Entry):PartialEntry

  abstract override def getReceivers(entry:Entry) = {
    val k = extractKey(entry)
    log.debug("using key: " + k)
    val currentCache = cache.get
    val cfg = currentCache.get(k) match {
      case Some(v) =>
        log.debug("got a configuration hit: " + k + " -> " + v)
        v
      case None =>
        log.debug("cache miss, constraining configuration now for: " + k)
        val v = configuration.constrain(k)
        log.debug("constrained configuration is: " + v)

        // If we can cache this new configuration (because no one else has updated the cache), then do.  If not,
        // don't worry about it.  It will get recalculated and possibly cached next time it's used.
        cache.compareAndSet(currentCache,currentCache + (k -> v))
        v
    }
    cfg.findReceivers(entry)
  }

  abstract override def onConfigurationChange() {
    super.onConfigurationChange()
    cache.set(Map())
  }
}

object ConfigurationCache {
  object Attribute extends Enumeration {
    val Logger = Value
    val Level = Value
    val Thread = Value
    val Tags = Value
  }

  def keyExtractor(entry:Entry,attributes:Attribute.Value*) = {
    def include[T](attribute:Attribute.Value,value:T):Option[T] =
      if ( attributes.contains(attribute) )
        Some(value)
      else
        None

    import Attribute._
    PartialEntry(logger = include(Logger,entry.logger),
                 level = include(Level,entry.level),
                 thread = include(Thread,entry.thread),
                 tags = include(Tags,entry.tags))
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
