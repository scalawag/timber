package org.scalawag.timber.bridge.slf4j

import org.slf4j.ILoggerFactory
import org.scalawag.timber.api.style.slf4j
import org.scalawag.timber.api.impl.DefaultEntryDispatcherLoader

object Slf4jBridgeLoggerFactory extends ILoggerFactory {
  var factory = new slf4j.LoggerFactory(DefaultEntryDispatcherLoader.dispatcher)

  def getLogger(name:String):org.slf4j.Logger = new Slf4jBridgeLoggerAdapter(factory.getLogger(name))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
