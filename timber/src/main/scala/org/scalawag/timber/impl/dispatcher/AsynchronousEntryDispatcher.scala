package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.api._
import actors.{Actor, TIMEOUT}
import org.scalawag.timber.impl.Entry
import org.scalawag.timber.impl.{ImmutableVertex, InternalLogging, DefaultConfiguration}
import org.scalawag.timber.impl.receiver.EntryReceiver

class AsynchronousEntryDispatcher[T <: Logger](private val initialConfiguration: Configuration = DefaultConfiguration) extends Configurable with EntryDispatcher with InternalLogging {
  private val dispatcher = (new MessageDispatcher).start

  configuration = initialConfiguration

  def dispatch(entry: Entry) {
    sendToDispatcher(entry)
  }

  private def sendToDispatcher(msg: Any) {
    dispatcher ! msg
    if (dispatcher.getState == Actor.State.Terminated)
      dispatcher.restart
  }

  protected def getReceivers(entry: Entry): Set[EntryReceiver] = configuration.findReceivers(entry)

  private class MessageDispatcher extends Actor with InternalLogging {
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
          getReceivers(entry).foreach(_.receive(entry))
        case TIMEOUT =>
          log.debug("dispatcher thread exiting due to inactivity (%d ms)".format(shutdownTimeout))
          exit
        case x => throw new RuntimeException("invalid message received: " + x)
      }
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
