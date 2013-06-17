package org.scalawag.timber.impl.receiver

import java.util.concurrent.locks.{Lock, ReentrantLock}
import org.scalawag.timber.api.impl.Entry

trait ThreadSafe extends EntryReceiver {
  private val lock:Lock = new ReentrantLock

  abstract override def receive(entry: Entry) {
    lock.lock
    try {
      super.receive(entry)
    } finally {
      lock.unlock
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
