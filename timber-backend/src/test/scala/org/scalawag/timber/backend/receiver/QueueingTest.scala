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

import scala.language.reflectiveCalls

import org.scalatest.time.{Second, Seconds, Span}
import org.scalawag.timber.backend.receiver.concurrency.Queueing
import org.scalatest.concurrent.Eventually
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OneInstancePerTest

class QueueingTest extends AnyFunSpec with Matchers with OneInstancePerTest with Eventually {
  override implicit def patienceConfig = (PatienceConfig(Span(5, Seconds), Span(1, Second)))

  private val delay = 50L
  private val entry = new Entry()

  private class TestReceiver extends Receiver {
    var calls = 0

    def receive(entry: Entry) {
      Thread.sleep(delay)
      calls += 1
    }

    override def flush(): Unit = {}

    override def close(): Unit = {}
  }

  private val r = new TestReceiver
  private val qr = Queueing(r)

  it("should receive multiple entries and return immediately") {
    val time = timer(10) {
      qr.receive(entry)
    }

    time should be < (delay)
    r.calls should be < 10

    eventually {
      r.calls shouldBe 10
    }
  }

  def timer(iters: Int)(fn: => Unit) = {
    val start = System.currentTimeMillis
    (1 to iters).foreach(_ => fn)
    ((System.currentTimeMillis - start).toDouble / 1000.0 / iters).toLong
  }
}
