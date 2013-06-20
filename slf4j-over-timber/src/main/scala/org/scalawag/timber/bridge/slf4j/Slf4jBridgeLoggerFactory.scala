package org.scalawag.timber.bridge.slf4j

import org.slf4j.ILoggerFactory
import org.scalawag.timber.api.LoggerFactory
import org.scalawag.timber.api.style.slf4j.Logger
import java.util.concurrent.atomic.AtomicReference

object Slf4jBridgeLoggerFactory extends ILoggerFactory {
  // TODO: Need a default LoggerFactory here (instead of null)
  private var factoryReference = new AtomicReference[LoggerFactory[Logger]](null)

  def factory = factoryReference.get
  def factory_=(factory:LoggerFactory[Logger]):Unit = factoryReference.set(factory)

  def getLogger(name:String):org.slf4j.Logger = new Slf4jBridgeLoggerAdapter(factoryReference.get.getLogger(name))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
