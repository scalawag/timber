package org.scalawag.timber.api.backend.slf4j

import org.scalawag.timber.impl.dispatcher.EntryDispatcher
import org.slf4j.{MarkerFactory, LoggerFactory}
import org.scalawag.timber.api.impl.Entry

class Slf4jEntryDispatcher extends EntryDispatcher {
  import org.scalawag.timber.api.slf4j.Logging.Level._
  import org.scalawag.timber.api.Level.Implicits._

  def dispatch(entry:Entry) {
    val logger = LoggerFactory.getLogger(entry.logger)
    val message = entry.message
    val level = entry.level.level

    entry.tags.headOption.map( tag => MarkerFactory.getMarker(tag.getClass.getName) ) match {

      case Some(marker) =>
        if ( level >= ERROR ) {
          if ( logger.isErrorEnabled )
            logger.error(marker,message.text)
        } else if ( level > WARN ) {
          if ( logger.isWarnEnabled )
            logger.warn(marker,message.text)
        } else if ( level > INFO ) {
          if ( logger.isInfoEnabled )
            logger.info(marker,message.text)
        } else if ( level > DEBUG ) {
          if ( logger.isDebugEnabled )
            logger.debug(marker,message.text)
        } else {
          if ( logger.isTraceEnabled )
            logger.trace(marker,message.text)
        }

      case None =>
        if ( level >= ERROR ) {
          if ( logger.isErrorEnabled )
            logger.error(message.text)
        } else if ( level > WARN ) {
          if ( logger.isWarnEnabled )
            logger.warn(message.text)
        } else if ( level > INFO ) {
          if ( logger.isInfoEnabled )
            logger.info(message.text)
        } else if ( level > DEBUG ) {
          if ( logger.isDebugEnabled )
            logger.debug(message.text)
        } else {
          if ( logger.isTraceEnabled )
            logger.trace(message.text)
        }

    }

  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */