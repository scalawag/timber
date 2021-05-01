package org.scalawag.timber.backend.receiver.formatter

import org.scalawag.timber.api.Entry

object MessageOnlyEntryFormatter extends EntryFormatter {
  override def format(value: Entry) = value.message.map(_.text).getOrElse("")
}
