package org.scalawag.timber.dsl

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.impl.PartialEntry
import org.scalawag.timber.api.Level

class HighestLevelConditionTestSuite extends FunSuite with Matchers {
  private val level = 4
  private val c = new HighestLevelCondition(level)

  test("disallow when level is higher than specified") {
    c.allows(entry(level + 1)) shouldBe Some(false)
  }

  test("allow when level is that specified") {
    c.allows(entry(level)) shouldBe Some(true)
  }

  test("allow when level is lower than specified") {
    c.allows(entry(level - 1)) shouldBe Some(true)
  }

  test("reconfigure") {
    val newThreshold = 8
    val newCondition = c.reconfigure(newThreshold)
    newCondition.getClass shouldBe classOf[HighestLevelCondition]
    newCondition.threshold shouldBe newThreshold
  }

  test("read parameter") {
    c.parameterValue shouldBe level
  }

  test("abstain when level is absent") {
    c.allows(PartialEntry()) shouldBe None
  }

  private def entry(level:Int) = new PartialEntry(level = Some(Level(level)))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
