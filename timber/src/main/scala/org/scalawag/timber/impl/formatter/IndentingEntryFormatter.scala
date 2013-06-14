package org.scalawag.timber.impl.formatter

import org.scalawag.timber.impl.Entry

/** This custom formatter prefixes every line in the log with the header information. It prefixes
  * each new log with a plus sign (+) in the first column so that you can see where new entries begin.
  */

class IndentingEntryFormatter extends EntryFormatter {
  private val newline = System.getProperty("line.separator")

  def format(entry: Entry): String = {
    val header = Traversable(
      DefaultEntryFormatter.defaultTimestampFormatterFactory().format(entry.timestamp),
      entry.levelName,
      entry.logger,
      entry.thread.getName,
      entry.location.map(_.toString).getOrElse(""),
      entry.tags.mkString(","),
      entry.context.map{ case (k,v) => "%s=%s".format(k,v.head) }.mkString(",")
    ).mkString("","|","|")

    entry.message.lines.mkString("+" + header,newline + " " + header,newline)
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */