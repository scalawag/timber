package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.Entry
import java.util.concurrent.locks.{Lock, ReentrantLock}

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
