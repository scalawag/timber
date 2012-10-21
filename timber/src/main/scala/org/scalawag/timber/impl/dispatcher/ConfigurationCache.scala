package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.Entry
import org.scalawag.timber.impl.{InternalLogging, ImmutableVertex, PartialEntry}

trait ConfigurationCache extends EntryDispatcher with Configurable with InternalLogging {
  var cache = Map[PartialEntry,Configuration]()

  def extractKey(entry:Entry):PartialEntry

  abstract override def getReceivers(entry:Entry) = {
    val k = extractKey(entry)
    log.debug("using key: " + k)
    val cfg = cache.get(k) match {
      case Some(v) =>
        log.debug("got a configuration hit: " + k + " -> " + v)
        v
      case None =>
        log.debug("cache miss, constraining configuration now for: " + k)
        val v = configuration.constrain(k)
        log.debug("constrained configuration is: " + v)
        cache += (k -> v)
        v
    }
    cfg.findReceivers(entry)
  }

  abstract override def onConfigurationChange() {
    super.onConfigurationChange()
    cache = Map() // TODO: This need to be thread-safe for use in the SynchronousLoggerManager, for Asynch it's OK
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
