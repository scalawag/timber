package org.scalawag.timber

import impl.dispatcher.Configuration
import impl.formatter.DefaultEntryFormatter
import impl.receiver.{AutoFlush, StderrReceiver, Asynchronous}

package object impl {

  // This needs to be moved to another package (so it doesn't get initialized with the rest of the stuff in here)
  lazy val DefaultConfiguration: Configuration = new ImmutableReceiver(Asynchronous(new StderrReceiver(new DefaultEntryFormatter) with AutoFlush))

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
