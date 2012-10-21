package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.api.Tag
import org.scalawag.timber.impl.PartialEntry

class TaggedConditionTestSuite extends FunSuite with ShouldMatchers {
  object t1 extends Tag
  object t2 extends Tag
  val c = new TaggedCondition(t1)

  test("disallow when tags is empty") {
    c.allows(entry(Set())) should be (Some(false))
  }

  test("disallow when tags does not contain tag") {
    c.allows(entry(Set(t2))) should be (Some(false))
  }

  test("allow when tags contains tag") {
    c.allows(entry(Set(t1))) should be (Some(true))
  }

  test("allow when tags contains multiple tags including tag") {
    c.allows(entry(Set(t1,t2))) should be (Some(true))
  }

  test("abstain when tags is absent") {
    c.allows(PartialEntry()) should be (None)
  }

  private def entry(tags:Set[Tag]) = new PartialEntry(tags = Some(tags))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
