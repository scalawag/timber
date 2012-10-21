package org.scalawag.timber.impl.factory

import org.scalawag.timber.api.{Logger, LoggerFactory}
import org.scalawag.timber.impl.InternalLogging

trait BoundedLoggerCache[T <: Logger] extends LoggerFactory[T] with InternalLogging {
  protected lazy val loggerCacheSize = 1024
  private lazy val loggers = new LRUMap[String,T](loggerCacheSize)

  abstract override def getLogger(name:String):T = {
    loggers.synchronized {
      loggers.get(name)(super.getLogger)
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
