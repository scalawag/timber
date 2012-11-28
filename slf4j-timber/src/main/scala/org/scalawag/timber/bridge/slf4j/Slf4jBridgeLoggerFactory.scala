package org.scalawag.timber.bridge.slf4j

import org.slf4j.ILoggerFactory
import org.scalawag.timber.api.LoggerFactory
import org.scalawag.timber.api.slf4j.Logger
import java.util.concurrent.atomic.AtomicReference

object Slf4jBridgeLoggerFactory extends ILoggerFactory {
  private var factoryReference = new AtomicReference[LoggerFactory[Logger]](org.scalawag.timber.api.slf4j.LoggerManager)

  def factory = factoryReference.get
  def factory_=(factory:LoggerFactory[Logger]):Unit = factoryReference.set(factory)

  def getLogger(name:String):org.slf4j.Logger = new Slf4jBridgeLoggerAdapter(factoryReference.get.getLogger(name))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
