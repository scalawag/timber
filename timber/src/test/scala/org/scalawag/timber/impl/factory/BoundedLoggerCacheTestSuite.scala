package org.scalawag.timber.impl.factory

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.api.{LoggerFactory, Logger}

class BoundedLoggerCacheTestSuite extends FunSuite with Matchers {

  private class MyLoggerFactory extends LoggerFactory[Logger] {
    var calls = 0
    def getLogger(name:String):Logger = {
      calls += 1
      new Logger(name,null) // dispatch won't matter if we never actually log (and we don't)
    }
  }

  test("cache prevents second call to super.getLogger with same name") {

    val lf = new MyLoggerFactory with BoundedLoggerCache[Logger]

    val l1 = lf.getLogger("foo")

    lf.calls shouldBe 1

    val l2 = lf.getLogger("foo")

    lf.calls shouldBe 1
    l1 shouldBe l2
  }

  test("cache doesn't prevent second call to super.getLogger with different name") {

    val lf = new MyLoggerFactory with BoundedLoggerCache[Logger]

    val l1 = lf.getLogger("foo")

    lf.calls shouldBe 1

    val l2 = lf.getLogger("bar")

    lf.calls shouldBe 2
    l1 should not be (l2)
  }

  test("cache only keeps N loggers") {
    val lf = new MyLoggerFactory with BoundedLoggerCache[Logger] {
      override protected lazy val loggerCacheSize = 1
    }

    val l1 = lf.getLogger("foo") // This should put "foo" into the cache

    lf.calls shouldBe 1

    val l2 = lf.getLogger("foo") // This should be a cache hit

    lf.calls shouldBe 1
    l1 shouldBe l2

    val l3 = lf.getLogger("bar") // This should eject "foo" from the cache and insert "bar"

    lf.calls shouldBe 2

    val l4 = lf.getLogger("bar") // This should be a cache hit

    lf.calls shouldBe 2
    l3 shouldBe l4

    val l5 = lf.getLogger("foo") // This should have to create a new logger due to the ejection above

    lf.calls shouldBe 3
    l5 should not be (l1)
    l5.name shouldBe l1.name
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
