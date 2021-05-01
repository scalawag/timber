// timber -- Copyright 2012-2021 -- Justin Patterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalawag.timber.backend.receiver

import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.InternalLogger
import org.scalawag.timber.backend.receiver.buffering.{ImmediateFlushing, PeriodicFlushing}
import org.scalawag.timber.backend.receiver.concurrency.{Locking, Queueing}
import sun.misc.{Signal, SignalHandler}

import scala.concurrent.duration.FiniteDuration

/** Marks a class that is capable of receiving [[Entry entries]] and doing something interesting with them. What
  * exactly it does is up to the implementation, but common behaviors are writing to a local log file or sending
  * to a log aggregation service.
  */

trait Receiver {

  /** Receives an [[Entry]] for processing.  There is no initialization method for receivers, so this method must
    * handle resource initialization on the first call after construction or after closure.
    *
    * @param entry the entry being received by this receiver
    */
  def receive(entry: Entry): Unit

  /** Tells this receiver to flush any buffered entries that it has received but not yet fully processed.  This makes
    * it possible for this receiver to improve performance by batching I/O work.
    */
  def flush(): Unit

  /** Closes the receiver.  This means that the resources held by this receiver should be released in preparation for
    * it being decommissioned. This usually happens before system shutdown, but it does not necessarily mean that this
    * receiver will not receive any more entries.  Receviers can be closed simply to release file system or network
    * resources.
    *
    * This method must handle any flushing that is required prior to any resource deallocation. Timber will not
    * necessarily call `flush()` before `close()`.
    */
  def close(): Unit

  def flushImmediately: Receiver = ImmediateFlushing(this)
  def flushAtLeastEvery(duration: FiniteDuration): Receiver = PeriodicFlushing(this, duration)

  def withLocking: Receiver = Locking(this)
  def withQueueing: Receiver = Queueing(this)
}

/** Provides methods for telling timber how to manage your Receivers. */

object Receiver {
  private[this] var closeOnShutdownReceivers: Set[Receiver] = Set.empty
  private[this] var shutdownHookInstalled = false

  /** Registers [[Receiver receivers]] to be closed at system shutdown.  This uses the Java runtime's
    * [[java.lang.Runtime#addShutdownHook addShutdownHook]] which only executes during certain system shutdown
    * scenarios.
    *
    * Calling this method multiple times with the same receiver has no effect beyond calling it once.
    *
    * @param receivers a set of receivers to attempt to close on normal system shutdown
    */
  def closeOnShutdown(receivers: Receiver*): Unit = {
    if (!shutdownHookInstalled) {
      val runnable = new Runnable {
        override def run(): Unit = {
          closeOnShutdownReceivers foreach { er =>
            try {
              er.close()
            } catch {
              // $COVERAGE-OFF$
              case ex: Exception =>
                InternalLogger.error(s"failed to close entry receiver ($er) at shutdown: $ex")
              // $COVERAGE-ON$
            }
          }
        }
      }

      Runtime.getRuntime.addShutdownHook(new Thread(runnable, "Timber-ReceiverManager-ShutdownHook"))
    }

    closeOnShutdownReceivers ++= receivers
  }

  // TODO: This is non-standard Java.  We should maybe protect it with reflection to allow this to run on non-Oracle
  // TODO: JVMs.  Or maybe you just can't use this call except on Oracle JVMs.

  private[this] var closeOnSignalReceivers: Map[String, Set[Receiver]] = Map.empty

  /** Registers [[Receiver receivers]] to be closed when the JVM receives a certain signal.  This is intended for
    * integration with tools like [[https://github.com/logrotate/logrotate logrotate]] that are capable of sending
    * signals during rotation to tell the logging process to close and reopen its log files to prevent the process
    * from hanging on to an open file handle and therefore continuing to log to a log file that has been rotated out.
    *
    * Calling this method multiple times with the same receiver and signal combination has no effect beyond calling
    * it once with that combination.  All receivers passed to calls for the same signal are accumulated and closed
    * when that signal is received.  There is no way to retract the registration.
    *
    * @param signal the signal to listen for (I recommend "HUP")
    * @param receivers the receivers to close when the signal is received
    */
  def closeOnSignal(signal: String, receivers: Receiver*): Unit = {
    // Ony install the signal handler once.  After that, just add the receivers to the set.
    if (!closeOnSignalReceivers.contains(signal)) {
      Signal.handle(
        new Signal(signal),
        new SignalHandler {
          override def handle(s: Signal) = {
            closeOnSignalReceivers(signal) foreach { er =>
              try {
                er.close()
              } catch {
                case ex: Exception =>
                  InternalLogger.error(s"failed to close receiver ($er) on signal $signal: $ex")
              }
            }
          }
        }
      )
    }

    val newReceiversSet = closeOnSignalReceivers.get(signal).getOrElse(Set.empty) ++ receivers
    closeOnSignalReceivers += signal -> newReceiversSet
  }
}
