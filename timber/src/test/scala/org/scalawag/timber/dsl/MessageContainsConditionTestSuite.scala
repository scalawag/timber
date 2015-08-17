package org.scalawag.timber.dsl

import org.scalatest.{Matchers,FunSuite}
import java.util.regex.Pattern
import org.scalawag.timber.impl.PartialEntry

class MessageContainsConditionTestSuite extends FunSuite with Matchers {

  test("disallow when message does not contain literal string") {
    new MessageContainsCondition("TARGET").allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message contains literal string") {
    new MessageContainsCondition("TARGET").allows(entry("blah to my TARGET thing")) shouldBe Some(true)
  }

  test("allow when message IS the literal string") {
    new MessageContainsCondition("TARGET").allows(entry("TARGET")) shouldBe Some(true)
  }

  test("disallow when message does not contain pattern") {
    new MessageContainsCondition("a\\d{1,4}a").allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message contains pattern") {
    new MessageContainsCondition("a\\d{1,4}a").allows(entry("something a45able")) shouldBe Some(true)
  }

  test("disallow when message does not contain compiled pattern") {
    new MessageContainsCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message contains compiled pattern") {
    new MessageContainsCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("something a45able")) shouldBe Some(true)
  }

  test("disallow when message does not contain regex") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message contains regex") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(entry("something a45able")) shouldBe Some(true)
  }

  test("abstain when message is absent") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(PartialEntry()) shouldBe None
  }

  private def entry(s:String) = new PartialEntry(message = Some(s))
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
