package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.impl.PartialEntry

class LoggerStartsWithTestSuite extends FunSuite with ShouldMatchers {
  val c = new LoggerPrefixCondition("org.scalawag")

  test("disallow when logger does not start with string") {
    c.allows(entry("blah")) should be (Some(false))
  }

  test("allow when logger matches the string") {
    c.allows(entry("org.scalawag")) should be (Some(true))
  }

  test("allow when logger starts with the string") {
    c.allows(entry("org.scalawag.timber")) should be (Some(true))
  }

  test("disallow when logger contains string (not at start)") {
    c.allows(entry("blah.org.scalawag")) should be (Some(false))
  }

  test("abstain when logger is absent") {
    c.allows(PartialEntry()) should be (None)
  }

  private def entry(logger:String) = new PartialEntry(logger = Some(logger))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
