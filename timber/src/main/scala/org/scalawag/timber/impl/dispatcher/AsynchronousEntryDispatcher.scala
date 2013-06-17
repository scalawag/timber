package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl.NamedThreadFactory
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit, ThreadPoolExecutor}
import org.scalawag.timber.api.impl.Entry

class AsynchronousEntryDispatcher extends EntryDispatcher {

  private val executor = new ThreadPoolExecutor(0,1,500L,TimeUnit.MILLISECONDS,
                                                new LinkedBlockingQueue[Runnable],
                                                new NamedThreadFactory(this.toString))

  def dispatch(entry:Entry) = {
    executor.submit(new Runnable {
      def run() {
        getReceivers(entry).foreach(_.receive(entry))
      }
    })
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
