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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.backend.dispatcher.EntryFacets
import org.scalawag.timber.backend.dispatcher.configuration.DogmaticConditions

class LogicalOperationConditionTest extends AnyFunSpec with Matchers with DogmaticConditions {

  private def descriptionOf(result: Option[Boolean]) =
    result match {
      case Some(true)  => "match"
      case Some(false) => "not match"
      case None        => "abstain"
    }

  describe("not") {
    def test(c: Condition, expected: Option[Boolean]): Unit = {
      it(s"should ${descriptionOf(expected)} for $c") {
        (!c).accepts(EntryFacets.Empty) shouldBe expected
      }
    }

    test(TRUE, Some(false))
    test(FALSE, Some(true))
    test(ABSTAIN, None)

    it("should have the right string representation") {
      (!TRUE).toString shouldBe s"not($TRUE)"
    }
  }

  describe("and") {
    def test(l: Condition, r: Condition, expected: Option[Boolean]): Unit = {
      it(s"should ${descriptionOf(expected)} for $l and $r") {
        (l and r).accepts(EntryFacets.Empty) shouldBe expected
      }

      it(s"should ${descriptionOf(expected)} for $l && $r") {
        (l && r).accepts(EntryFacets.Empty) shouldBe expected
      }
    }

    test(TRUE, TRUE, Some(true))
    test(FALSE, TRUE, Some(false))
    test(TRUE, FALSE, Some(false))
    test(FALSE, FALSE, Some(false))
    test(TRUE, ABSTAIN, None)
    test(FALSE, ABSTAIN, None)
    test(ABSTAIN, TRUE, None)
    test(ABSTAIN, FALSE, None)
    test(ABSTAIN, ABSTAIN, None)

    it("should have the right string representation") {
      (TRUE and FALSE).toString shouldBe s"($TRUE) and ($FALSE)"
    }

    it("should allow '&&' instead of 'and'") {
      val l = level < 5
      val r = level < 7
      (l && r) shouldBe (l and r)
    }
  }

  describe("or") {
    def test(l: Condition, r: Condition, expected: Option[Boolean]): Unit = {
      it(s"should ${descriptionOf(expected)} for $l and $r") {
        (l or r).accepts(EntryFacets.Empty) shouldBe expected
      }
    }

    test(TRUE, TRUE, Some(true))
    test(FALSE, TRUE, Some(true))
    test(TRUE, FALSE, Some(true))
    test(FALSE, FALSE, Some(false))
    test(TRUE, ABSTAIN, None)
    test(FALSE, ABSTAIN, None)
    test(ABSTAIN, TRUE, None)
    test(ABSTAIN, FALSE, None)
    test(ABSTAIN, ABSTAIN, None)

    it("should have the right string representation") {
      (TRUE or FALSE).toString shouldBe s"($TRUE) or ($FALSE)"
    }

    it("should allow '||' instead of 'or'") {
      val l = level < 5
      val r = level < 7
      (l || r) shouldBe (l or r)
    }
  }
}
