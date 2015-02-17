package org.scalawag.timber.dsl

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.api.Message
import java.util.regex.Pattern
import org.scalawag.timber.impl.PartialEntry

import Message._

class MessageMatchesConditionTestSuite extends FunSuite with Matchers {

  test("disallow when message does not match literal string") {
    new MessageMatchesCondition("TARGET").allows(entry("blah")) shouldBe Some(false)
  }

  test("disallow when message contains literal string but does not match") {
    new MessageMatchesCondition("TARGET").allows(entry("blah to my TARGET thing")) shouldBe Some(false)
  }

  test("allow when message matches the literal string") {
    new MessageMatchesCondition("TARGET").allows(entry("TARGET")) shouldBe Some(true)
  }

  test("disallow when message does not match pattern") {
    new MessageMatchesCondition("a\\d{1,4}a").allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message matches pattern") {
    new MessageMatchesCondition("a\\d{1,4}a").allows(entry("a678a")) shouldBe Some(true)
  }

  test("disallow when message does not match compiled pattern") {
    new MessageMatchesCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message matches compiled pattern") {
    new MessageMatchesCondition(Pattern.compile("a\\d{1,4}a")).allows(entry("a678a")) shouldBe Some(true)
  }

  test("disallow when message does not match regex") {
    new MessageMatchesCondition("a\\d{1,4}a".r).allows(entry("blah")) shouldBe Some(false)
  }

  test("allow when message matches regex") {
    new MessageMatchesCondition("a\\d{1,4}a".r).allows(entry("a678a")) shouldBe Some(true)
  }

  test("abstain when message is absent") {
    new MessageContainsCondition("a\\d{1,4}a".r).allows(PartialEntry()) shouldBe None
  }

  private def entry(s:String) = new PartialEntry(message = Some(s))

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
