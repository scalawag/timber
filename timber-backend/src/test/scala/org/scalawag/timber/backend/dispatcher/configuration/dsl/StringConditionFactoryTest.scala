// timber -- Copyright 2012-2021 -- Justin Patterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalawag.timber.backend.dispatcher.configuration.dsl

import java.util.regex.Pattern

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.backend.dispatcher.EntryFacets

class StringConditionFactoryTest extends AnyFunSpec with Matchers with MockFactory {
  val extractFn = mockFunction[EntryFacets, Option[Iterable[String]]]("extractFn")
  val entry = mock[EntryFacets]
  val scf = StringConditionFactory("testLabel")(extractFn)

  describe("string literal pattern") {
    new Fixture {
      val pattern: StringOrPattern = "[Tt]e{0,1}s[Tt]".r
      declareTests()
    }
  }

  describe("scala Regex pattern") {
    new Fixture {
      val pattern: StringOrPattern = "[Tt]e{0,1}s[Tt]".r
      declareTests()
    }
  }

  describe("Java Pattern pattern") {
    new Fixture {
      val pattern: StringOrPattern = Pattern.compile("[Tt][Ee][Ss][Tt]")
      declareTests()
    }
  }

  private trait Fixture {
    val pattern: StringOrPattern

    def declareTests(): Unit = {

      describe("===") {
        val condition = scf === pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(false))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(false))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(false))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel === $pattern"""
        }
      }

      describe("is") {
        val condition = scf is pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(false))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(false))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(false))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel is $pattern"""
        }
      }

      describe("matches") {
        val condition = scf matches pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(false))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(false))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(false))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel matches $pattern"""
        }
      }

      describe("contains") {
        val condition = scf contains pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(true))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(true))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(true))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel contains $pattern"""
        }
      }

      describe("startsWith") {
        val condition = scf startsWith pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(true))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(false))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(false))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel startsWith $pattern"""
        }
      }

      describe("endsWith") {
        val condition = scf endsWith pattern

        declareTest("is the pattern", condition, Some(Iterable("Test")), Some(true))
        declareTest("starts with the pattern", condition, Some(Iterable("TestCase")), Some(false))
        declareTest("ends with the pattern", condition, Some(Iterable("UnitTest")), Some(true))
        declareTest("contains the pattern", condition, Some(Iterable("BetaTester")), Some(false))
        declareTest("is empty", condition, Some(Iterable.empty), Some(false))
        declareTest("is absent", condition, None, None)

        it("should have the right string representation") {
          condition.toString shouldBe s"""testLabel endsWith $pattern"""
        }
      }

    }
  }

  private def declareTest(
      description: String,
      condition: Condition,
      extraction: Option[Iterable[String]],
      expected: Option[Boolean]
  ) {
    val result = expected match {
      case None        => "abstain"
      case Some(true)  => "accept"
      case Some(false) => "reject"
    }

    it(s"should $result when extraction $description") {
      extractFn.expects(*).returns(extraction)
      condition.accepts(entry) shouldBe expected
    }
  }
}
