package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.Entry

trait AutoFlush extends WriterReceiver {
  abstract override def receive(entry: Entry) {
    super.receive(entry)
    writer.flush
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
