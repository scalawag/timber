package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.api.Message._
import java.util.regex.Pattern
import org.scalawag.timber.impl.PartialEntry

class MessageMatchesConditionTestSuite extends FunSuite with ShouldMatchers {

  test("disallow when message does not match literal string") {
    new MessageMatchesCondition("TARGET").allows(entry("blah")) should be (Some(false))
  }

  test("disallow when message contains literal string but does not match") {
    new MessageMatchesCondition("TARGET").allows(entry("blah to my TARGET thing")) should be (Some(false))
  }

  test("allow when message matches the literal string") {
    new MessageMatchesCondition("TARGET").allows(entry("TARGET")) should be (Some(true))
  }

  test("disallow when message does not match pattern") {
    new MessageMatchesCondition("a\\d{1,4}a").allows(entry("blah")) should be (Some(false))
  }

  test("allow when message matches pattern") {
    new MessageMatchesCondition("a\\d{1,4}a").allows(entry("a678a")) should be (Some(true))
  }

  test("disallow when message does not match compiled pattern") {
    new MessageMatchesCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("blah")) should be (Some(false))
  }

  test("allow when message matches compiled pattern") {
    new MessageMatchesCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("a678a")) should be (Some(true))
  }

  test("disallow when message does not match regex") {
    new MessageMatchesCondition("a\\d{1,4}a".r).allows(entry("blah")) should be (Some(false))
  }

  test("allow when message matches regex") {
    new MessageMatchesCondition("a\\d{1,4}a".r).allows(entry("a678a")) should be (Some(true))
  }

  test("abstain when message is absent") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(PartialEntry()) should be (None)
  }

  private def entry(s:String) = new PartialEntry(message = Some(s))

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
