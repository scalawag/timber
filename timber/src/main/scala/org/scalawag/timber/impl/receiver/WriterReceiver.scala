package org.scalawag.timber.impl.receiver

import java.io.{OutputStreamWriter, OutputStream, PrintStream, Writer}
import org.scalawag.timber.impl.formatter.EntryFormatter

class WriterReceiver(protected[this] val writer: Writer,
                     override protected[this] val formatter: EntryFormatter) extends FormattingReceiver(formatter)

object OutputStreamReceiver {
  def makeWriter(out: OutputStream, charset: Option[String]) =
    charset match {
      case Some(csn) => new OutputStreamWriter(out, csn)
      case None => new OutputStreamWriter(out)
    }
}

class OutputStreamReceiver(override protected[this] val formatter: EntryFormatter,
                           protected[this] val out: PrintStream,
                           protected[this] val charset: Option[String] = None)
  extends WriterReceiver(OutputStreamReceiver.makeWriter(out, charset), formatter)

class StdoutReceiver(override protected[this] val formatter: EntryFormatter,
                     override protected[this] val charset: Option[String] = None)
  extends OutputStreamReceiver(formatter, System.out, charset) {
  override val toString = "stdout"
}

class StderrReceiver(override protected[this] val formatter: EntryFormatter,
                     override protected[this] val charset: Option[String] = None)
  extends OutputStreamReceiver(formatter, System.err, charset) {
  override val toString = "stderr"
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
