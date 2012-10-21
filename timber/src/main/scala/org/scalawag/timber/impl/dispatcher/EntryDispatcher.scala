package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.Entry
import org.scalawag.timber.impl.receiver.EntryReceiver

trait EntryDispatcher {
  def dispatch(entry:Entry)
  protected def getReceivers(entry:Entry):Set[EntryReceiver]
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
