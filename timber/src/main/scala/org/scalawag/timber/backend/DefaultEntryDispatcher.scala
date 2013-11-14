package org.scalawag.timber.backend

import scala.reflect.runtime.universe
import org.scalawag.timber.impl.dispatcher.{Configuration, SynchronousEntryDispatcher}
import org.scalawag.timber.impl.InternalLogging
import scala.reflect.internal.MissingRequirementError
import org.scalawag.timber.impl.formatter.{EntryFormatter, DefaultEntryFormatter}
import org.scalawag.timber.impl.receiver.{EntryReceiver, AutoFlush, StderrReceiver}
import org.scalawag.timber.api.impl._

trait TimberConfiguration {
  import org.scalawag.timber.dsl._

  protected[this] lazy val formatter:EntryFormatter = new DefaultEntryFormatter
  protected[this] lazy val receiver:EntryReceiver = new StderrReceiver(formatter) with AutoFlush
  protected[this] lazy val configuration:Configuration = Configuration(receiver)
  protected[this] lazy val unconfiguredDispatcher:org.scalawag.timber.impl.dispatcher.EntryDispatcher = new SynchronousEntryDispatcher

  lazy val dispatcher = {
    val disp = unconfiguredDispatcher
    disp.configuration = configuration
    disp
  }
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
