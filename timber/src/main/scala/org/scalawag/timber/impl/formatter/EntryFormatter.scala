package org.scalawag.timber.impl.formatter

import org.scalawag.timber.api.impl.Entry

trait EntryFormatter {
  def format(entry: Entry): String
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
