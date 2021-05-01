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

package org.scalawag.timber.backend.receiver.concurrency

import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.SingleThreadExecutionContext
import org.scalawag.timber.backend.receiver.Receiver

import scala.concurrent.{ExecutionContext, Future}

/** Prevents concurrent access to the core Receiver methods through queueing the entries and processing them
  * with a single-threaded ExecutionContext.
  */

class Queueing(delegate: Receiver) extends Receiver {
  implicit private[this] lazy val ec: ExecutionContext = SingleThreadExecutionContext(this.toString)

  override def receive(entry: Entry): Unit = Future(delegate.receive(entry))
  override def flush(): Unit = Future(delegate.flush())
  override def close(): Unit = Future(delegate.close())

  override val toString: String = s"Queueing($delegate)"
}

object Queueing {
  def apply(delegate: Receiver): Queueing = new Queueing(delegate)
}
