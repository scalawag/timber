package org.scalawag.timber.api.impl

import scala.reflect.runtime.universe
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher
import org.scalawag.timber.impl.InternalLogging
import scala.reflect.internal.MissingRequirementError

trait TimberConfiguration {
  val dispatcher:EntryDispatcher
}

object DefaultEntryDispatcher extends EntryDispatcher with InternalLogging {

  lazy val dispatcher:EntryDispatcher = customDispatcher.getOrElse(defaultDispatcher)

  private[this] def defaultDispatcher = {
    log.debug("No custom EntryDispatcher found, using default")
    new SynchronousEntryDispatcher
  }

  private[this] def customDispatcher = {
    log.debug("Attempting to load custom EntryDispatcher from Timber")
    val classLoader = Thread.currentThread.getContextClassLoader // TODO: ClassLoader.getSystemClassLoader?

    val rootMirror = universe.runtimeMirror(classLoader)
    try {
      val driverSymbol = rootMirror.staticModule("Timber")
      val driverMirror = rootMirror.reflectModule(driverSymbol)
      Some(driverMirror.instance.asInstanceOf[TimberConfiguration].dispatcher)
    } catch {
      case ex:MissingRequirementError =>
        log.debug("Unable to find an object called 'Timber'")
        None
      case ex:ClassCastException =>
        log.error(s"Object 'Timber' does not subclass ${classOf[TimberConfiguration].getClass.getName}")
        None
    }
  }

  def dispatch(entry:Entry) = dispatcher.dispatch(entry)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
