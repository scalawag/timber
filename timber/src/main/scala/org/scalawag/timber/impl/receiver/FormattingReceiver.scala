package org.scalawag.timber.impl.receiver

import java.io.Writer
import org.scalawag.timber.impl.formatter.EntryFormatter
import org.scalawag.timber.api.impl.Entry

abstract class FormattingReceiver(protected[this] val formatter: EntryFormatter) extends EntryReceiver {
  protected[this] def writer:Writer
  override def receive(entry: Entry) = writer.write(formatter.format(entry))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
