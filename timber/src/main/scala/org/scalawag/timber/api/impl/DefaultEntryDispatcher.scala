package org.scalawag.timber.api.impl

import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher

object DefaultEntryDispatcher extends EntryDispatcher {
  var dispatcher:EntryDispatcher = new SynchronousEntryDispatcher

  def dispatch(entry:Entry) = dispatcher.dispatch(entry)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
