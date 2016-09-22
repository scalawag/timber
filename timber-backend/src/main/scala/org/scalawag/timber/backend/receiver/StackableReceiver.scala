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

package org.scalawag.timber.backend.receiver

import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.buffering.{BufferingPolicy, LazyFlushing}
import org.scalawag.timber.backend.receiver.concurrency.{ConcurrencyPolicy, NoThreadSafety}

/** A Receiver that allows the core behavior (what the receiver does) to be stacked with a
  * [[org.scalawag.timber.backend.receiver.buffering.BufferingPolicy BufferingPolicy]] and a
  * [[org.scalawag.timber.backend.receiver.concurrency.ConcurrencyPolicy ConcurrencyPolicy]], provided by timber.
  *
  * You can take advantage of the policy stacking in two ways.  You can either create your receivers as extensions of
  * [[Receiver]] and then instantiate them like this:
  *
  * {{{
  *   import org.scalawag.timber.api._
  *   import org.scalawag.timber.backend.receiver._
  *   import org.scalawag.timber.backend.receiver.buffering.PeriodicFlushing
  *   import org.scalawag.timber.backend.receiver.concurrency.Locking
  *
  *   class MyReceiver extends Receiver {
  *     override def receive(entry: Entry) = ???
  *     override def flush() = ???
  *     override def close() = ???
  *   }
  *
  *   val r = new StackableReceiver(new MyReceiver) with Locking with PeriodicFlushing
  * }}}
  *
  * You can, alternatively extends StackableReceiver directly, in which case your instantiation code looks a bit nicer.
  *
  * {{{
  *   import org.scalawag.timber.api._
  *   import org.scalawag.timber.backend.receiver._
  *   import org.scalawag.timber.backend.receiver.buffering.PeriodicFlushing
  *   import org.scalawag.timber.backend.receiver.concurrency.Locking
  *
  *   class MyReceiver extends StackableReceiver(new Receiver {
  *     override def receive(entry: Entry) = ???
  *     override def flush() = ???
  *     override def close() = ???
  *   })
  *
  *   val r = new MyReceiver with Locking with PeriodicFlushing
  * }}}
  *
  * The downside is that your receiver can't be subclassed as easily.  To get the best of both worlds, it may be wise
  * to produce a pair of classes: one that extends [[Receiver]] and one that extends [[StackableReceiver]].
  *
  * {{{
  *   import org.scalawag.timber.api._
  *   import org.scalawag.timber.backend.receiver._
  *   import org.scalawag.timber.backend.receiver.buffering.PeriodicFlushing
  *   import org.scalawag.timber.backend.receiver.concurrency.Locking
  *
  *   class MyReceiver extends Receiver {
  *     override def receive(entry: Entry) = ???
  *     override def flush() = ???
  *     override def close() = ???
  *   }

  *   class MyStackableReceiver extends StackableReceiver(new MyReceiver)
  *
  *   val r = new MyStackableReceiver with Locking with PeriodicFlushing
  * }}}
  *
  * @param coreBehavior the essential behavior of this receiver (i.e., what it does with entries)
  */

// Downside of stackable is that it can't be subclassed easily.  If you want it to be subclassed, you should make
// a non-stackable EntityReceiver (maybe paired with a Stackable version for instantiation if it can be used directly.

class StackableReceiver(final protected[this] val coreBehavior:Receiver) extends Receiver {
  private[backend] def bufferingPolicy:BufferingPolicy = LazyFlushing
  private[backend] def concurrencyPolicy:ConcurrencyPolicy = NoThreadSafety

  private[this] lazy val delegate = concurrencyPolicy.layerConcurrencyBehavior(bufferingPolicy.layerBufferingBehavior(coreBehavior))

  final override def receive(entry:Entry) = delegate.receive(entry)
  final override def flush() = delegate.flush()
  final override def close() = delegate.close()
}
