package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.Entry
import java.io.Writer
import org.scalawag.timber.impl.formatter.EntryFormatter

abstract class FormattingReceiver(protected[this] val formatter: EntryFormatter) extends EntryReceiver {
  protected[this] def writer:Writer
  override def receive(entry: Entry) = writer.write(formatter.format(entry))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
