package org.scalawag.timber.impl.receiver

//import language.reflectiveCalls

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalawag.timber.api.Level
import org.scalawag.timber.api.impl.Entry

class AsynchronousTestSuite extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  private val delay = 500L
  private val entry = new Entry("foo","logger",Level(0))

  private val r = new EntryReceiver {
    var calls = 0

    def receive(entry: Entry) {
      Thread.sleep(delay)
      calls += 1
    }
  }

  private val ar = Asynchronous(r)

  test("receive multiple entries and return immediately") {
    val time = timer(1) {
      ar.receive(entry)
    }

    time should be < ( delay )
    r.calls should be (0)

    var maxTries = 10
    while ( r.calls == 0 && maxTries > 0 ) {
      Thread.sleep(delay)
      maxTries -= 1
    }

    r.calls should be (1)
  }

  def timer(iters:Int)(fn: => Unit) = {
    val start = System.currentTimeMillis
    (1 to iters).foreach(_ => fn)
    ( ( System.currentTimeMillis - start ).toDouble / 1000.0 / iters ).toLong
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
