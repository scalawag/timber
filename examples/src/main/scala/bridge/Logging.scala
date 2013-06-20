package bridge

import org.scalawag.timber.api
import org.scalawag.timber.api.style.slf4j
import org.scalawag.timber.api.style.slf4j._
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher
import org.scalawag.timber.bridge.slf4j.Slf4jBridgeLoggerFactory

object Logging {

  object EntryDispatcher extends SynchronousEntryDispatcher

  object LoggerFactory extends slf4j.LoggerFactory(EntryDispatcher) {
    override def getLogger(name:String) = super.getLogger(name.reverse)
  }

  /* This tells the slf4j bridge to use our factory here instead of the default one.  It must be called by the
   * application prior to any slf4j loggers being created (for homogeneous Loggers).
   */

  def configurate {
    Slf4jBridgeLoggerFactory.factory = LoggerFactory
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
