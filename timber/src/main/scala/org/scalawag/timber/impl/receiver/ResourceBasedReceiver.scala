package org.scalawag.timber.impl.receiver

import java.io.Writer
import org.scalawag.timber.impl.InternalLogging

trait ResourceBasedReceiver extends EntryReceiver with InternalLogging {
  protected def open:Writer
  private var isClosed = false

  protected def writer:Writer = resource match {
    case Some(r) =>
      r
    case None =>
      if ( isClosed )
        throw new IllegalStateException("underlying resource has already been closed")

      log.debug("Opening underlying resource: " + this)
      resource = Some(open)
      resource.get
  }

  private var resource:Option[Writer] = None

  def close = {
    resource match {
      case Some(r) =>
        log.debug("Closing underlying resource: " + this)
        r.close
        resource = None
      case None =>
        // noop
    }
    isClosed = true
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
