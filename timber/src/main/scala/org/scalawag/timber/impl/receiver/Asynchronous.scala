package org.scalawag.timber.impl.receiver

import scala.actors.{TIMEOUT, Actor}
import org.scalawag.timber.impl.InternalLogging
import org.scalawag.timber.impl.Entry

class Asynchronous(private val receiver:EntryReceiver) extends EntryReceiver { self =>

  override def receive(entry: Entry) {
    MessageDispatcher ! entry
    MessageDispatcher.getState match {
      case Actor.State.New => MessageDispatcher.start
      case Actor.State.Terminated => MessageDispatcher.restart
      case _ => // noop
    }
  }

  private object MessageDispatcher extends Actor with InternalLogging {
    private var shutdownTimeout = 100

    override def start(): Actor = {
      log.debug("dispatcher thread starting")
      super.start
    }

    override def restart() {
      log.debug("dispatcher thread restarting")
      super.restart
    }

    def act = loop {
      reactWithin(shutdownTimeout) {
        case entry: Entry =>
          self.receiver.receive(entry)
        case TIMEOUT =>
          log.debug("dispatcher thread exiting due to inactivity (%d ms)".format(shutdownTimeout))
          exit
        case x =>
          log.warn("invalid message received: " + x)
      }
    }
  }

  override lazy val toString = "Async(%s)".format(receiver)
}

object Asynchronous {
  def apply(receiver:EntryReceiver) = new Asynchronous(receiver)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
