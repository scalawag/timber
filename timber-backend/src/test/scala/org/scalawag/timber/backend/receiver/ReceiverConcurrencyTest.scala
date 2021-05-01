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

package org.scalawag.timber.backend.receiver

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Span, Seconds}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.buffering.ImmediateFlushing
import org.scalawag.timber.backend.receiver.concurrency.{Locking, Queueing}
import scala.concurrent._
import scala.concurrent.Future._
import scala.concurrent.Await._
import annotation.tailrec
import java.util.concurrent.{BrokenBarrierException, TimeoutException, TimeUnit, CyclicBarrier}

import scala.util.Try

class ReceiverConcurrencyTest extends AnyFunSpec with Matchers with Eventually {

  // Just useful for telling when two write() calls overlap in time.

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(5, Seconds), Span(1, Second))

  case class Call(method: String, start: Long, end: Long, result: Try[Int])

  private class TestReceiver extends Receiver {
    var calls = Seq.empty[Call]

    // This barrier forces three calls to run concurrently (if possible).  If it's not possible (which is the goal of
    // the test) it should fail in a way that lets us know that concurrency was attempted.
    private val barrier = new CyclicBarrier(3)

    private def doMethodCall(method: String) = {
      val start = System.nanoTime
      val result = Try(barrier.await(1, TimeUnit.SECONDS))
      val end = System.nanoTime

      calls.synchronized {
        calls :+= Call(method, start, end, result)
      }
    }

    override def receive(entry: Entry) = doMethodCall("receive")
    override def flush() = doMethodCall("flush")
    override def close() = doMethodCall("close")

    def hasOverlappingCalls: Boolean = {
      @tailrec
      def existsPair[A](items: Iterable[A])(fn: (A, A) => Boolean): Boolean = {
        @tailrec
        def existsPairHelper[A](first: A, rest: Iterable[A])(fn: (A, A) => Boolean): Boolean =
          if (rest.size > 0)
            fn(first, rest.head) || existsPairHelper(first, rest.tail)(fn)
          else
            false

        if (items.isEmpty)
          false
        else
          existsPairHelper(items.head, items.tail)(fn) || existsPair(items.tail)(fn)
      }

      def overlaps(l: Call, r: Call): Boolean =
        if (l.start < r.start)
          l.end >= r.start
        else if (r.start < l.start)
          r.end >= l.start
        else
          true

      calls.synchronized {
        existsPair(calls)(overlaps)
      }
    }
  }

  private val entry = new Entry()

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  it("should not protect Receiver with no concurrency wrapper mixed in (control case)") {
    val r = new TestReceiver

    verifyConcurrencyAllowed(r, r)
  }

  it("should protect Receiver with Locking") {
    val r = new TestReceiver
    val lr = Locking(r)

    verifyConcurrencyDisallowed(r, lr)
  }

  it("should protect Receiver with Queueing") {
    val r = new TestReceiver
    val qr = Queueing(r)

    verifyConcurrencyDisallowed(r, qr)
  }

  private def verifyConcurrencyAllowed(testReceiver: TestReceiver, concurrentReceiver: Receiver): Unit = {
    val result1 = Future(concurrentReceiver.receive(entry))
    val result2 = Future(concurrentReceiver.flush())
    val result3 = Future(concurrentReceiver.close())

    ready(sequence(Seq(result1, result2, result3)), Duration.Inf)

    eventually {
      testReceiver.calls.size shouldBe 3

      // We should have gotten one of each call (regardless of the order).

      testReceiver.calls.map(_.method).toSet shouldBe Set("receive", "flush", "close")

      // All calls should have succeeded because they all made it to the barrier in time.

      testReceiver.calls.forall(_.result.isSuccess) shouldBe true

      // Call times should overlap without concurrency protection.

      testReceiver.hasOverlappingCalls shouldBe true
    }
  }

  private def verifyConcurrencyDisallowed(testReceiver: TestReceiver, concurrentReceiver: Receiver): Unit = {
    val result1 = Future(concurrentReceiver.receive(entry))
    val result2 = Future(concurrentReceiver.flush())
    val result3 = Future(concurrentReceiver.close())

    ready(sequence(Seq(result1, result2, result3)), Duration.Inf)

    eventually {
      testReceiver.calls.size shouldBe 3

      // We should have gotten one of each call (regardless of the order).

      testReceiver.calls.map(_.method).toSet shouldBe Set("receive", "flush", "close")

      // The first call should have timed out.  The last two should have run into the broken (timed out) barrier.

      testReceiver.calls(0).result.isFailure shouldBe true
      testReceiver.calls(0).result.failed.get.isInstanceOf[TimeoutException] shouldBe true

      testReceiver.calls(1).result.isFailure shouldBe true
      testReceiver.calls(1).result.failed.get.isInstanceOf[BrokenBarrierException] shouldBe true

      testReceiver.calls(2).result.isFailure shouldBe true
      testReceiver.calls(2).result.failed.get.isInstanceOf[BrokenBarrierException] shouldBe true

      // In no case should the calls have overlapped due to our concurrency protection.

      testReceiver.hasOverlappingCalls shouldBe false
    }
  }
}
