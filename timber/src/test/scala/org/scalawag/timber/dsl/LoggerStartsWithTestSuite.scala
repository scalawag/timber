package org.scalawag.timber.dsl

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.impl.PartialEntry

class LoggerStartsWithTestSuite extends FunSuite with Matchers {
  val c = new LoggerPrefixCondition("org.scalawag")

  test("disallow when logger does not start with string") {
    c.allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when logger matches the string") {
    c.allows(entry("org.scalawag")) shouldBe Some(true)
  }

  test("allow when logger starts with the string") {
    c.allows(entry("org.scalawag.timber")) shouldBe Some(true)
  }

  test("disallow when logger contains string (not at start)") {
    c.allows(entry("blah.org.scalawag")) shouldBe Some(false)
  }

  test("abstain when logger is absent") {
    c.allows(PartialEntry()) shouldBe None
  }

  private def entry(logger:String) = new PartialEntry(logger = Some(logger))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
