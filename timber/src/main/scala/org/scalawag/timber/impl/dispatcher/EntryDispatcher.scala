package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.receiver.EntryReceiver
import org.scalawag.timber.api.impl.Entry
import org.scalawag.timber.api

trait EntryDispatcher extends api.impl.EntryDispatcher with Configurable {
  protected def getReceivers(entry:Entry):Set[EntryReceiver] = configuration.findReceivers(entry)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
