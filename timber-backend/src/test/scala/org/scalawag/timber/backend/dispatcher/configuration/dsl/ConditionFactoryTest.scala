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

class ConditionFactoryTest extends AnyFunSpec with Matchers with MockFactory {
  val extractFn = mockFunction[EntryFacets,Option[Iterable[Any]]]("extractFn")
  val entry = mock[EntryFacets]
  val scf = new ConditionFactory("testLabel",extractFn)

  describe("isPresent") {
    val condition = scf isPresent

    it("should accept when extraction is present") {
      extractFn.expects(*).returns(Some(Some("u")))
      condition.accepts(entry) shouldBe Some(true)
    }

    it("should reject when extraction is absent") {
      extractFn.expects(*).returns(Some(None))
      condition.accepts(entry) shouldBe Some(false)
    }

    it("should abstain when extraction is unknown") {
      extractFn.expects(*).returns(None)
      condition.accepts(entry) shouldBe None
    }

    it("should have the right string representation") {
      condition.toString shouldBe "testLabel isPresent"
    }
  }

  describe("isAbsent") {
    val condition = scf isAbsent

    it("should accept when extraction is present") {
      extractFn.expects(*).returns(Some(Some("u")))
      condition.accepts(entry) shouldBe Some(false)
    }

    it("should reject when extraction is absent") {
      extractFn.expects(*).returns(Some(None))
      condition.accepts(entry) shouldBe Some(true)
    }

    it("should abstain when extraction is unknown") {
      extractFn.expects(*).returns(None)
      condition.accepts(entry) shouldBe None
    }

    it("should have the right string representation") {
      condition.toString shouldBe "testLabel isAbsent"
    }
  }

}
