package org.scalawag.timber.impl.formatter

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date
import org.scalawag.timber.api.impl.Entry

object DefaultEntryFormatter {

  trait TimestampFormatter {
    def format(timestamp:Long):String
  }

  type TimestampFormatterFactory = Function0[TimestampFormatter]

  class DateFormatTimestampFormatter(val dateFormat:DateFormat) extends TimestampFormatter {
    def format(timestamp: Long):String = dateFormat.format(new Date(timestamp))
  }

  def memoizeThreadLocal[A](fn:Function0[A]):Function0[A] = new ThreadLocalMemoizer[A](fn)

  class ThreadLocalMemoizer[A](fn:Function0[A]) extends Function0[A] {
    private val cache = new ThreadLocal[A]

    def apply(): A = cache.get match {
      case null =>
        val v = fn()
        cache.set(v)
        v
      case v =>
        v
    }
  }

  val defaultTimestampFormatterFactory = memoizeThreadLocal[TimestampFormatter]({ () =>
    new DateFormatTimestampFormatter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))
  })
/*
  private val formatters = ThreadLocal[DateFormatter]

  private[DefaultEntryFormatter] getFormatter(factory:DateFormatFactory) = {
  }
*/
}

import DefaultEntryFormatter._

class DefaultEntryFormatter(timestampFormatterFactory:DefaultEntryFormatter.TimestampFormatterFactory = DefaultEntryFormatter.defaultTimestampFormatterFactory,
                            delimiter:String = "|",
                            headerOnEachLine:Boolean = false,
                            useLevelName:Boolean = true)
  extends EntryFormatter
{
  private val newline = System.getProperty("line.separator")
  def format(entry: Entry): String = {
    val header = Traversable(
      timestampFormatterFactory().format(entry.timestamp),
      if ( useLevelName ) entry.level.toString else entry.level.level,
      entry.logger,
      entry.thread.getName,
      entry.location.map(_.toString).getOrElse(""),
      entry.tags.mkString(","),
      entry.context.map{ case (k,v) => "%s=%s".format(k,v.head) }.mkString(",")
    ).mkString("",delimiter,delimiter)

    def prepend = "%s%s".format(header,_:String)

    if ( headerOnEachLine )
      entry.message.lines.map(prepend).mkString(newline) + newline
    else
      prepend(entry.message.text) + newline
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
