package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.api.Logger
import org.scalawag.timber.api.impl.Entry

class SynchronousEntryDispatcher extends EntryDispatcher {
  def dispatch(entry: Entry) {
    getReceivers(entry).foreach(_.receive(entry))
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
