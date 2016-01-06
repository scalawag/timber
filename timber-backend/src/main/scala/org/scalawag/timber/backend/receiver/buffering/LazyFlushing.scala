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

import org.scalawag.timber.backend.receiver.{StackableReceiver, DelegatingReceiver, Receiver}

/** Applies the LazyFlushing (see companion object) buffering policy to the [[StackableReceiver]] it is mixed into. */
trait LazyFlushing { _: StackableReceiver =>
  private[backend] final override val bufferingPolicy = LazyFlushing
}

/** Disables automatic flushing of a [[Receiver]]. */
object LazyFlushing extends BufferingPolicy {
  private[backend] override def layerBufferingBehavior(delegate:Receiver) = new DelegatingReceiver(delegate)
}
