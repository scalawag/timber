package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.{InternalLogging, Entry}
import org.scalawag.timber.impl.receiver.EntryReceiver

trait EntryDispatcher extends Configurable {
  def dispatch(entry:Entry)
  protected def getReceivers(entry:Entry):Set[EntryReceiver] = configuration.findReceivers(entry)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
