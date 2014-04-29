package org.scalawag.timber.slf4j.receiver.logback

import ch.qos.logback.core.Appender
import org.scalawag.timber.impl.receiver.EntryReceiver
import org.scalawag.timber.impl.InternalLogging
import org.scalawag.timber.api.impl.Entry

class AppenderAdapter(private[logback] val appender:Appender[Entry]) extends EntryReceiver with InternalLogging {
  def receive(entry: Entry) {
    appender.doAppend(entry)
  }
}

trait StopAtShutdown { self:AppenderAdapter =>
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run() {
      log.debug("calling close on " + self)
      self.appender.stop
    }
  })
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
