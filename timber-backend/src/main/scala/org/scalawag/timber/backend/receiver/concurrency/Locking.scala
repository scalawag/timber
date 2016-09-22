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

package org.scalawag.timber.backend.receiver.concurrency

import java.util.concurrent.locks.{Lock, ReentrantLock}
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.{StackableReceiver, Receiver}

/** Applies the Locking (see companion object) concurrency policy to the [[StackableReceiver]] it is mixed into. */

trait Locking { _: StackableReceiver =>
  final override private[backend] val concurrencyPolicy = Locking
}

/** Prevents concurrent access to the core Receiver methods through locking (with a [[ReentrantLock]]). */

object Locking extends ConcurrencyPolicy {
  override def layerConcurrencyBehavior(delegate:Receiver) = new Behavior(delegate)

  class Behavior(delegate:Receiver) extends Receiver {
    private[this] val lock:Lock = new ReentrantLock
  
    private[this] def withLock(fn: => Unit):Unit = {
      lock.lock
      try {
        fn
      } finally {
        lock.unlock
      }
    }
  
    override def receive(entry: Entry) = withLock(delegate.receive(entry))
    override def flush() = withLock(delegate.flush())
    override def close() = withLock(delegate.close())
    override val toString = s"Locking(${delegate.toString()})"
  }
}

