package org.scalawag.timber

import impl.dispatcher.Configuration
//import impl.dispatcher.Configuration._
import impl.formatter.DefaultEntryFormatter
import impl.receiver.{Asynchronous, OutputStreamReceiver}

package object impl {

  // This needs to be moved to another package (so it doesn't get initialized with the rest of the stuff in here)
  lazy val DefaultConfiguration: Configuration = new ImmutableReceiver(Asynchronous(new OutputStreamReceiver(new DefaultEntryFormatter,System.err)))

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
