package org.scalawag.timber.impl.factory

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalawag.timber.api.{LoggerFactory, Logger}
import org.scalawag.timber.impl.LoggerImpl

class UnboundedLoggerCacheTestSuite extends FunSuite with ShouldMatchers with MockitoSugar {

  private class MyLoggerFactory extends LoggerFactory[Logger] {
    var calls = 0
    def getLogger(name:String):Logger = {
      calls += 1
      new LoggerImpl(name,null) // dispatch won't matter if we never actually log (and we don't)
    }
  }

  test("cache prevents second call to super.getLogger with same name") {
    val lf = new MyLoggerFactory with UnboundedLoggerCache[Logger]

    val l1 = lf.getLogger("foo")

    lf.calls should be (1)

    val l2 = lf.getLogger("foo")

    lf.calls should be (1)
    l1 should be (l2)
  }

  test("cache doesn't prevent second call to super.getLogger with different name") {
    val lf = new MyLoggerFactory with UnboundedLoggerCache[Logger]

    val l1 = lf.getLogger("foo")

    lf.calls should be (1)

    val l2 = lf.getLogger("bar")

    lf.calls should be (2)
    l1 should not be (l2)
  }

  test("cache keeps all loggers") {
    val lf = new MyLoggerFactory with UnboundedLoggerCache[Logger]

    val l1 = lf.getLogger("foo") // This should put "foo" into the cache

    lf.calls should be (1)

    val l2 = lf.getLogger("foo") // This should be a cache hit

    lf.calls should be (1)
    l1 should be (l2)

    val l3 = lf.getLogger("bar") // This should eject "foo" from the cache and insert "bar"

    lf.calls should be (2)

    val l4 = lf.getLogger("bar") // This should be a cache hit

    lf.calls should be (2)
    l3 should be (l4)

    val l5 = lf.getLogger("foo") // This should have to create a new logger due to the ejection above

    lf.calls should be (2)
    l5 should be (l1)
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
