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
import org.scalatest.concurrent.Eventually
import org.scalatest.time.Span
import org.scalatest.{Matchers, FunSpec}
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.WriterBasedStackableReceiver
import org.scalawag.timber.backend.receiver.formatter.DefaultEntryFormatter
import org.scalatest.time._
import scala.concurrent.duration._

class PeriodicFlushingTest extends FunSpec with Matchers with Eventually {
  override implicit def patienceConfig = PatienceConfig(Span(1,Second),Span(100,Milliseconds))

  class FakeWriter extends Writer {
    var flushCount = 0
    var writeCount = 0
    override def flush() = { flushCount += 1 }
    override def write(cbuf: Array[Char], off: Int, len: Int) = { writeCount += 1 }
    override def close() = {}
  }

  private val oneLineEntry = new Entry(message = Some("foo"))
  private val twoLineEntry = new Entry(message = Some("foo\nbar"))

  it("should not flush immediately after each receive") {
    val writer = new FakeWriter
    val receiver = new WriterBasedStackableReceiver(writer)
    (0 until 10).foreach(_ => receiver.receive(oneLineEntry))

    writer.writeCount shouldBe 10
    writer.flushCount shouldBe 0
  }

  it("should flush on receive eventually") {
    val writer = new FakeWriter
    val receiver = new WriterBasedStackableReceiver(writer) with PeriodicFlushing {
      override protected[this] val maximumFlushInterval  = 500.milliseconds
    }
    (0 until 10).foreach(_ => receiver.receive(oneLineEntry))

    writer.writeCount shouldBe 10
    writer.flushCount shouldBe 0

    eventually {
      writer.writeCount shouldBe 10
      writer.flushCount shouldBe 1
    }
  }

  it("should restart interval timer after manual flush") {
    val writer = new FakeWriter
    val receiver = new WriterBasedStackableReceiver(writer) with PeriodicFlushing {
      override protected[this] val maximumFlushInterval  = 1.second
    }
    receiver.receive(oneLineEntry)
    Thread.sleep(800) // almost the flush interval
    receiver.receive(oneLineEntry)
    receiver.flush() // manual flush

    writer.writeCount shouldBe 2
    writer.flushCount shouldBe 1

    Thread.sleep(300) // the automated flush would have happened by now if the manual one hadn't

    writer.flushCount shouldBe 1
  }

  it("should not flush if nothing's been received") {
    val writer = new FakeWriter
    val receiver = new WriterBasedStackableReceiver(writer) with PeriodicFlushing {
      override protected[this] val maximumFlushInterval  = 50.milliseconds
    }
    Thread.sleep(100) // the automated flush would have happened by now if anything had been received
  }

  it("should flush before close") {
    val writer = new FakeWriter
    val receiver = new WriterBasedStackableReceiver(writer) with PeriodicFlushing {
      override protected[this] val maximumFlushInterval  = 50.milliseconds
    }
    receiver.receive(oneLineEntry)
    writer.writeCount shouldBe 1
    writer.flushCount shouldBe 0

    receiver.close()

    writer.writeCount shouldBe 1
    writer.flushCount shouldBe 1
  }
}
