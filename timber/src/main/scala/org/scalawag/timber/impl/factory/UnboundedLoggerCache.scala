package org.scalawag.timber.impl.factory

import org.scalawag.timber._
import org.scalawag.timber.api.{Logger, LoggerFactory}
import impl.InternalLogging

trait UnboundedLoggerCache[T <: Logger] extends LoggerFactory[T] with InternalLogging {
  private var loggers = Map[String,T]()

  abstract override def getLogger(name:String):T = {
    loggers.synchronized {
      loggers.get(name) match {
        case Some(logger) =>
          log.debug("Using cached logger for %s".format(name))
          logger
        case None =>
          log.debug("Creating new logger for %s".format(name))
          val logger = super.getLogger(name)
          loggers += (name -> logger)
          logger
      }
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
