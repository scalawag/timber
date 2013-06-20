package org.scalawag.timber.impl

import formatter.DefaultEntryFormatter
import org.scalawag.timber.impl.dispatcher.{SynchronousEntryDispatcher, EntryDispatcher}
import org.scalawag.timber.impl.receiver._
import org.scalawag.timber.dsl.LowestLevelCondition
import org.scalawag.timber.api
import org.scalawag.timber.api.style.slf4j
import org.scalawag.timber.api.impl.Entry

/** The type of Logger that timber uses internally. */

class InternalLogger(override val name:String,override val dispatcher:EntryDispatcher)
  extends api.Logger(name,dispatcher) with slf4j.Debug with slf4j.Warn with slf4j.Error

/** The factory used by timber internally.  It's important that this not try to use any logging itself. */

object InternalLoggerManager extends EntryDispatcher with api.LoggerFactory[InternalLogger] {
  override def getLogger(name:String) = new InternalLogger(name,this)

  // This is the configuration that's always used by timber internally.  It always writes to stderr.  It will
  // limit the printed log entries to those with level WARN or above unless the system property "timber.debug"
  // is set to something non-empty.  If that's the case, it will log everything.
  //
  // It's important that the building of this configuration not attempt to use any logging.  That's why I'm using
  // the classes directly instead of the configuration DSL here.

  configuration = {
    val threshold =
      if ( Option(System.getProperty("timber.debug")).exists( _.length > 0 ) )
        api.Level.DEBUG.level
      else
        api.Level.WARN.level
    val endpoint = new OutputStreamReceiver(new DefaultEntryFormatter, System.err) with AutoFlush with ThreadSafe
    val condition = new LowestLevelCondition(threshold)
    new ImmutableFilter(condition,Set(new ImmutableReceiver(endpoint)))
  }

  override def dispatch(entry:Entry) = getReceivers(entry).foreach(_.receive(entry))
}

/** This is the trait that internal classes mix in to get internal logging goodness. */

trait InternalLogging extends api.Logging[InternalLogger] {
  override protected val loggerFactory = InternalLoggerManager
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
