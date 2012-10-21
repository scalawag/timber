package org.scalawag.timber.impl.receiver

trait CloseAtShutdown { self:ResourceBasedReceiver =>
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run() {
      log.debug("calling close on " + self)
      self.close
    }
  })
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
