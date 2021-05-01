// timber -- Copyright 2012-2015 -- Justin Patterson
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

import scala.language.postfixOps

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.backend.dispatcher.EntryFacets

class IntConditionFactoryTest extends AnyFunSpec with Matchers with MockFactory {
  val extractFn = mockFunction[EntryFacets,Option[Iterable[Int]]]("extractFn")
  val entry = mock[EntryFacets]
  val scf = IntConditionFactory("testLabel")(extractFn)

  val target = 5

  describe("is") {
    val condition = scf is target

    declareTest("is the target",condition,Some(Some(target)),Some(true))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(false))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(false))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel is $target"""
    }
  }

  describe("===") {
    val condition = scf === target

    declareTest("is the target",condition,Some(Some(target)),Some(true))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(false))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(false))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel === $target"""
    }
  }

  describe(">") {
    val condition = scf > target

    declareTest("is the target",condition,Some(Some(target)),Some(false))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(false))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(true))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel > $target"""
    }
  }

  describe(">=") {
    val condition = scf >= target

    declareTest("is the target",condition,Some(Some(target)),Some(true))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(false))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(true))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel >= $target"""
    }
  }

  describe("<") {
    val condition = scf < target

    declareTest("is the target",condition,Some(Some(target)),Some(false))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(true))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(false))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel < $target"""
    }
  }

  describe("<=") {
    val condition = scf <= target

    declareTest("is the target",condition,Some(Some(target)),Some(true))
    declareTest("is less than the target",condition,Some(Some(target - 1)),Some(true))
    declareTest("is greater than the target",condition,Some(Some(target + 1)),Some(false))
    declareTest("is absent",condition,None,None)

    it("should have the right string representation") {
      condition.toString shouldBe s"""testLabel <= $target"""
    }
  }

  private def declareTest(description:String, condition: Condition, extraction:Option[Iterable[Int]], expected:Option[Boolean]) {
    val result = expected match {
      case None => "abstain"
      case Some(true) => "accept"
      case Some(false) => "reject"
    }

    it(s"should $result when extraction $description") {
      extractFn.expects(*).returns(extraction)
      condition.accepts(entry) shouldBe expected
    }
  }
}
