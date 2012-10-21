package org.scalawag.timber.slf4j.receiver.logback

import ch.qos.logback.core.encoder.EncoderBase
import org.scalawag.timber.impl.Entry
import org.scalawag.timber.impl.formatter.EntryFormatter
import org.scalawag.timber.impl.receiver.OutputStreamReceiver
import java.io.{Writer, OutputStream}

class EncoderAdapter(private val formatter:EntryFormatter,charset: Option[String] = None) extends EncoderBase[Entry] {
  private var writer:Writer = null

  override def init(os: OutputStream) {
    super.init(os)
    writer = OutputStreamReceiver.makeWriter(os,charset)
  }

  def doEncode(entry: Entry) = writer.write(formatter.format(entry))

  def close = writer.flush
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
