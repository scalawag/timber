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

import java.io.Writer
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.WriterBasedStackableReceiver
import org.scalawag.timber.backend.receiver.formatter.DefaultEntryFormatter

class LazyFlushingTest extends AnyFunSpec with Matchers with MockFactory {
  private val oneLineEntry = new Entry(message = Some("foo"))

  it("should allow explicit specification of lazy flushing") {
    val writer = mock[Writer]
    val receiver = new WriterBasedStackableReceiver(writer) with LazyFlushing

    (writer.write(_:String)).expects(*).repeat(10)
    (0 until 10).foreach(_ => receiver.receive(oneLineEntry))
  }
}
