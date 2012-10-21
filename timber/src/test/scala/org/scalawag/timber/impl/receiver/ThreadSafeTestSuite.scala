package org.scalawag.timber.impl.receiver

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import java.io.Writer
import org.scalawag.timber.impl.formatter.DefaultEntryFormatter
import org.scalawag.timber.impl.Entry
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import scala.concurrent.ops.par
import java.util.concurrent.atomic.AtomicReference
import annotation.tailrec
import java.util.concurrent.{TimeoutException, TimeUnit, CyclicBarrier}

class ThreadSafeTestSuite extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest {

  // Just useful for telling when two write() calls overlap in time.

  private class TestReceiver extends EntryReceiver {
    var callTimes = Seq[(Long,Long)]()
    val barrier = new CyclicBarrier(2)

    override def receive(entry:Entry) {
      val start = System.currentTimeMillis

      barrier.await(2,TimeUnit.SECONDS)

      val end = System.currentTimeMillis

      callTimes.synchronized {
        callTimes :+= (start,end)
      }
    }

    def hasOverlappingCalls:Boolean = {
      @tailrec
      def existsPair[A](items: Iterable[A])(fn: (A,A) => Boolean): Boolean = {
        @tailrec
        def existsPairHelper[A](first:A,rest:Iterable[A])(fn: (A,A) => Boolean): Boolean =
          if ( rest.size > 0 )
            fn(first,rest.head) || existsPairHelper(first,rest.tail)(fn)
          else
            false

        if ( items.isEmpty )
          false
        else
          existsPairHelper(items.head,items.tail)(fn) || existsPair(items.tail)(fn)
      }


      def overlaps(a:(Long,Long),b:(Long,Long)):Boolean =
        if ( a._1 < b._1 )
          a._2 >= b._1
        else if ( b._1 < a._1 )
          b._2 >= a._1
        else
          true

      callTimes.synchronized {
        existsPair(callTimes)(overlaps)
      }
    }
  }

  private val entry = new Entry("blah","logger",0,"DEBUG")

  test("receive without ThreadSafe (control case)") {
    val r = new TestReceiver

    par(r.receive(entry),r.receive(entry))

    r.callTimes.size should be (2)
    r.hasOverlappingCalls should be (true)
  }

  test("receive with ThreadSafe (test case)") {
    val r = new TestReceiver with ThreadSafe

    evaluating {
      par(r.receive(entry),r.receive(entry))
    } should produce [TimeoutException]
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
