package org.scalawag.timber.slf4j

import org.slf4j.ILoggerFactory

object Slf4jLoggerFactory extends ILoggerFactory {
  def getLogger(name:String):org.slf4j.Logger =
    new Slf4jLoggerAdapter(org.scalawag.timber.api.slf4j.LoggerManager.getLogger(name))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
