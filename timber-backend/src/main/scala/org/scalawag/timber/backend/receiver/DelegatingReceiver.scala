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

/** A Receiver that delegates all calls to another Receiver, specified at construction.
  *
  * @param delegate the Receiver to which this receiver delegates all calls
  */
class DelegatingReceiver(delegate:Receiver) extends Receiver {
  /** Calls the `receive(Entry)` method on the delegate receiver.
    *
    * @param entry the entry being received by this receiver
    */
  override def receive(entry: Entry) = delegate.receive(entry)

  /** Calls the `flush()` method on the delegate receiver. */
  override def flush() = delegate.flush()

  /** Calls the `close()` method on the delegate receiver. */
  override def close() = delegate.close()
}
