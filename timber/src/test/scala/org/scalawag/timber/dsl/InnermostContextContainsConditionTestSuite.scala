package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import collection.immutable.Stack
import org.scalawag.timber.impl.PartialEntry

class InnermostContextContainsConditionTestSuite extends FunSuite with ShouldMatchers {
  private val k0 = "k0"
  private val k1 = "k1"
  private val v0 = "v0"
  private val v1 = "v1"
  private val v2 = "v2"
  private val c = new InnermostContextEqualsCondition("k1","v1")

  test("disallow when other key contains the right value") {
    c.allows(entry(Map("k0" -> Stack("v1")))) should be (Some(false))
  }

  test("disallow when right key contains the wrong value") {
    c.allows(entry(Map("k1" -> Stack("v0")))) should be (Some(false))
  }

  test("disallow when right key contains value further down in the stack") {
    c.allows(entry(Map("k1" -> Stack("v0","v1")))) should be (Some(false))
  }

  test("disallow when key is not present") {
    c.allows(entry(Map())) should be (Some(false))
  }

  test("allow when the right key contains the right value") {
    c.allows(entry(Map("k1" -> Stack("v1","v0"),"k0" -> Stack("v2")))) should be (Some(true))
  }

  test("abstain when context is absent") {
    c.allows(PartialEntry()) should be (None)
  }

  private def entry(context:Map[String,Stack[String]]) = new PartialEntry(context = Some(context))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
