package org.scalawag.timber.dsl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.util.regex.Pattern
import org.scalawag.timber.impl.PartialEntry

class MessageContainsConditionTestSuite extends FunSuite with ShouldMatchers {

  test("disallow when message does not contain literal string") {
    new MessageContainsCondition("TARGET").allows(entry("blah")) should be (Some(false))
  }

  test("allow when message contains literal string") {
    new MessageContainsCondition("TARGET").allows(entry("blah to my TARGET thing")) should be (Some(true))
  }

  test("allow when message IS the literal string") {
    new MessageContainsCondition("TARGET").allows(entry("TARGET")) should be (Some(true))
  }

  test("disallow when message does not contain pattern") {
    new MessageContainsCondition("a\\d{1,4}a").allows(entry("blah")) should be (Some(false))
  }

  test("allow when message contains pattern") {
    new MessageContainsCondition("a\\d{1,4}a").allows(entry("something a45able")) should be (Some(true))
  }

  test("disallow when message does not contain compiled pattern") {
    new MessageContainsCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("blah")) should be (Some(false))
  }

  test("allow when message contains compiled pattern") {
    new MessageContainsCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("something a45able")) should be (Some(true))
  }

  test("disallow when message does not contain regex") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(entry("blah")) should be (Some(false))
  }

  test("allow when message contains regex") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(entry("something a45able")) should be (Some(true))
  }

  test("abstain when message is absent") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(PartialEntry()) should be (None)
  }

  private def entry(s:String) = new PartialEntry(message = Some(s))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
