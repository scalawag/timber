package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalawag.timber.impl.PartialEntry

class NotConditionTestSuite extends FunSuite with ShouldMatchers with MockitoSugar {
  private val entry = PartialEntry()

  test("disallow when condition says true") {
    val condition = mock[Condition]
    when(condition.allows(entry)).thenReturn(Some(true))

    val c = new NotCondition(condition)
    c.allows(entry) should be (Some(false))

    verify(condition,times(1)).allows(entry)
  }

  test("allow when condition says false") {
    val condition = mock[Condition]
    when(condition.allows(entry)).thenReturn(Some(false))

    val c = new NotCondition(condition)
    c.allows(entry) should be (Some(true))

    verify(condition,times(1)).allows(entry)
  }

  test("abstain when condition abstains") {
    val condition = mock[Condition]
    when(condition.allows(entry)).thenReturn(None)

    val c = new NotCondition(condition)
    c.allows(entry) should be (None)

    verify(condition,times(1)).allows(entry)
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
