package org.scalawag.timber.impl.formatter

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date
import org.scalawag.timber.api.impl.Entry

object DefaultEntryFormatter {

  trait TimestampFormatter {
    def format(timestamp:Long):String
  }

  type TimestampFormatterFactory = () => TimestampFormatter

  class DateFormatTimestampFormatter(val dateFormat:DateFormat) extends TimestampFormatter {
    def format(timestamp: Long):String = dateFormat.format(new Date(timestamp))
  }

  val defaultTimestampFormatterFactory = { () =>
    new DateFormatTimestampFormatter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))
  }

  private val newline = System.getProperty("line.separator")
}

import DefaultEntryFormatter._

class DefaultEntryFormatter(val timestampFormatterFactory:DefaultEntryFormatter.TimestampFormatterFactory = DefaultEntryFormatter.defaultTimestampFormatterFactory,
                            val delimiter:String = "|",
                            val headerOnEachLine:Boolean = false,
                            val indentToMatchHeaders:Boolean = false,
                            val useLevelName:Boolean = true,
                            val firstLinePrefix:String = "+",
                            val otherLinePrefix:String = " ")
  extends EntryFormatter
{
  private val formatters = new ThreadLocal[TimestampFormatter] {
    override def initialValue = timestampFormatterFactory()
  }

  def format(entry:Entry): String = {
    val header = Traversable(
      formatters.get.format(entry.timestamp),
      if ( useLevelName ) entry.level.toString else entry.level.level,
      entry.logger,
      entry.thread.getName,
      entry.location.map(_.toString).getOrElse(""),
      entry.tags.mkString(","),
      entry.context.map{ case (k,v) => "%s=%s".format(k,v.head) }.mkString(",")
    ).mkString("",delimiter,delimiter)

    val firstLineHeader = firstLinePrefix + header
    val otherLineHeader = newline + otherLinePrefix +
      (
        if ( headerOnEachLine )
          header
        else if ( indentToMatchHeaders )
          " " * header.length
        else
          ""
      )

    entry.message.lines.mkString(firstLineHeader,otherLineHeader,newline)
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
