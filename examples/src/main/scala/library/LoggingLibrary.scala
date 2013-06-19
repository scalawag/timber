package library

import org.scalawag.timber.api.jul.LoggerFactory

import org.scalawag.timber.api.Level.Implicits._

object LoggingLibrary {
  def go {
    val log = LoggerFactory.getLogger("BLAH")
    log.log(0,"blah")
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
