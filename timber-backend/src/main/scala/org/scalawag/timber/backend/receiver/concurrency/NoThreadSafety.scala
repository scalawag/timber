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

import org.scalawag.timber.backend.receiver.{StackableReceiver, DelegatingReceiver, Receiver}

/** Applies the NoThreadSafety (see companion object) concurrency policy to the [[StackableReceiver]] it is mixed into. */

trait NoThreadSafety { _: StackableReceiver =>
  final override private[backend] val concurrencyPolicy = NoThreadSafety
}

/** Allows unfettered concurrent access to the core Receiver methods. */

object NoThreadSafety extends ConcurrencyPolicy {
  override def layerConcurrencyBehavior(delegate:Receiver) = new DelegatingReceiver(delegate)
}
