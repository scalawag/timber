package org.scalawag.timber.impl.dispatcher

import java.util.concurrent.atomic.AtomicReference

import org.scalawag.timber.impl.{Entry, ImmutableVertex, InternalLogging, DefaultConfiguration}
import org.scalawag.timber.api.Logger
import org.scalawag.timber.impl.receiver.EntryReceiver

class SynchronousEntryDispatcher[T <: Logger] (private val initialConfiguration: Configuration = DefaultConfiguration) extends EntryDispatcher with Configurable with InternalLogging {
  configuration = initialConfiguration

  def dispatch(entry: Entry) {
    getReceivers(entry).foreach(_.receive(entry))
  }

  protected def getReceivers(entry:Entry): Set[EntryReceiver] = {
    configuration.findReceivers(entry)
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
