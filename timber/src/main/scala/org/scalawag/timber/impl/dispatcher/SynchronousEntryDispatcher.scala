package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.{Entry, InternalLogging, DefaultConfiguration}
import org.scalawag.timber.api.Logger

class SynchronousEntryDispatcher[T <: Logger] extends EntryDispatcher {
  def dispatch(entry: Entry) {
    getReceivers(entry).foreach(_.receive(entry))
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
