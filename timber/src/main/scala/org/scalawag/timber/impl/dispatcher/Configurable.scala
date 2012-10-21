package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.dsl.NameableValve
import java.util.concurrent.atomic.AtomicReference
import org.scalawag.timber.impl.{InternalLogging, DefaultConfiguration}

trait Configurable extends InternalLogging {
  private[this] val activeConfiguration = new AtomicReference(DefaultConfiguration)

  def configuration:Configuration = activeConfiguration.get()
  def configuration_=(configuration:Configuration) = {
    log.debug("resetting configuration to: " + configuration)
    activeConfiguration.set(configuration)
    onConfigurationChange
  }

  // Allows calling "configure" with a block to reset the configuration

  def configure(fn:NameableValve => Unit) {
    val IN = new NameableValve(true)
    fn(IN)
    configuration_=(IN)
  }

  // Override to update internal state on configuration change
  protected def onConfigurationChange() {}
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
