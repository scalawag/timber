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

package org.scalawag.timber.backend.receiver.buffering

import java.util.concurrent.{TimeUnit, _}
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.Receiver

import scala.concurrent.duration._

/** Periodically flushes a [[Receiver]] at a specified frequency. The receiver may flush more often than
  * this interval in other scenarios (e.g., when buffer capacity is reached or when expressly requested).
  *
  * The interval is dependent only on flushes that are initiated from the receiver, either explicitly through its
  * `flush()` method or automatically through the periodic schedule.  It does not take into account flushes that
  * occur in the underlying objects.
  *
  * @param maximumFlushInterval the maximum interval between flushes
  */

class PeriodicFlushing(delegate: Receiver, maximumFlushInterval: FiniteDuration = 5.seconds) extends Receiver {
  private[this] var scheduledFlush: Option[ScheduledFuture[_]] = None

  private[this] val flushRunnable = new Runnable {
    // Must use the external call so that there aren't concurrency issues where this thread has direct access to the
    // internal state without waiting in line for the single service thread to execute it.
    override def run(): Unit = flush()
  }

  private[this] def scheduleFlush(): Unit =
    if (scheduledFlush.isEmpty) {
      scheduledFlush = Some(
        PeriodicFlushing.executor.schedule(flushRunnable, maximumFlushInterval.toMillis, TimeUnit.MILLISECONDS)
      )
    }

  private[this] def cancelScheduledFlush(): Unit = {
    scheduledFlush foreach { sf =>
      sf.cancel(false)
      scheduledFlush = None
    }
  }

  override def receive(entry: Entry): Unit = {
    delegate.receive(entry)
    scheduleFlush()
  }

  override def flush(): Unit = {
    cancelScheduledFlush()
    delegate.flush()
  }

  override def close(): Unit = {
    delegate.flush()
    delegate.close()
  }
}

private[backend] object PeriodicFlushing {
  def apply(delegate: Receiver, maximumFlushInterval: FiniteDuration = 5.seconds): PeriodicFlushing =
    new PeriodicFlushing(delegate, maximumFlushInterval)

  private[PeriodicFlushing] val executor = {
    val threadFactory = new ThreadFactory { r =>
      override def newThread(r: Runnable): Thread =
        new Thread(r, "Timber-PeriodicFlusher")
    }
    Executors.newScheduledThreadPool(1, threadFactory)
  }
}
