package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.impl.PartialEntry
import org.scalawag.timber.api.Level

class LowestLevelConditionTestSuite extends FunSuite with ShouldMatchers {
  private val level = 4
  private val c = new LowestLevelCondition(level)

  test("disallow when level is lower than specified") {
    c.allows(entry(level - 1)) should be (Some(false))
  }

  test("allow when level is that specified") {
    c.allows(entry(level)) should be (Some(true))
  }

  test("allow when level is higher than specified") {
    c.allows(entry(level + 1)) should be (Some(true))
  }

  test("reconfigure") {
    val newThreshold = 8
    val newCondition = c.reconfigure(newThreshold)
    newCondition.getClass should be (classOf[LowestLevelCondition])
    newCondition.threshold should be (newThreshold)
  }

  test("read parameter") {
    c.parameterValue should be (level)
  }

  test("abstain when level is absent") {
    c.allows(PartialEntry()) should be (None)
  }

  private def entry(level:Int) = new PartialEntry(level = Some(Level(level)))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
