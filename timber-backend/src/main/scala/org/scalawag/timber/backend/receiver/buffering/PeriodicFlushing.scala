// timber -- Copyright 2012-2015 -- Justin Patterson
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
import org.scalawag.timber.backend.receiver.{StackableReceiver, Receiver}

import scala.concurrent.duration._

/** Applies the PeriodicFlushing (see companion object) buffering policy to the [[StackableReceiver]] it is mixed into.
  *
  * Override the `maximumFlushInterval` member to change the frequency of automatic flushes.
  */

trait PeriodicFlushing { _: StackableReceiver =>
  /** Determines the frequency at which the receiver is flushed. */
  protected[this] val maximumFlushInterval:FiniteDuration = PeriodicFlushingBehavior.DEFAULT_MAXIMUM_FLUSH_INTERVAL
  private[backend] final override def bufferingPolicy = PeriodicFlushing(maximumFlushInterval)
}

/** Periodically flushes a [[Receiver]] at the default frequency (5 seconds).  The receiver may flush more often than
  * this interval in other scenarios (e.g., when buffer capacity is reached).
  *
  * The interval is dependent only on flushes that are initiated from the receiver, either explicitly through its
  * `flush()` method or automatically through the periodic schedule.  It does not take into account flushes that
  * occur in the underlying objects.
  */

object PeriodicFlushing extends PeriodicFlushingPolicy() {
  /** Periodically flushes a [[Receiver]] at the specified interval.  The receiver may flush more often than
    * this interval in other scenarios (e.g., when buffer capacity is reached).
    *
    * @param maximumFlushInterval the maximum interval between flushes
    */
  def apply(maximumFlushInterval:FiniteDuration) = new PeriodicFlushingPolicy(maximumFlushInterval)
}

private[backend] class PeriodicFlushingPolicy(maximumFlushInterval:FiniteDuration = PeriodicFlushingBehavior.DEFAULT_MAXIMUM_FLUSH_INTERVAL) extends BufferingPolicy {
  private[backend] override def layerBufferingBehavior(delegate:Receiver) = new PeriodicFlushingBehavior(delegate,maximumFlushInterval)
}

private[backend] class PeriodicFlushingBehavior private[buffering] (delegate:Receiver, maximumFlushInterval:FiniteDuration) extends Receiver {
  private[this] var scheduledFlush:Option[ScheduledFuture[_]] = None

  private[this] val flushRunnable = new Runnable {
    // Must use the external call so that there aren't concurrency issues where this thread has direct access to the
    // internal state without waiting in line for the single service thread to execute it.
    override def run(): Unit = flush()
  }

  private[this] def scheduleFlush() =
    if ( scheduledFlush.isEmpty ) {
      scheduledFlush = Some(PeriodicFlushingBehavior.executor.schedule(flushRunnable,maximumFlushInterval.toMillis,TimeUnit.MILLISECONDS))
    }

  private[this] def cancelScheduledFlush() = {
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

private[backend] object PeriodicFlushingBehavior {
  val DEFAULT_MAXIMUM_FLUSH_INTERVAL = 5.seconds

  private val executor = {
    val threadFactory = new ThreadFactory {
      override def newThread(r:Runnable) = {
        new Thread(r,"Timber-PeriodicFlusher")
      }
    }
    Executors.newScheduledThreadPool(1,threadFactory)
  }
}
