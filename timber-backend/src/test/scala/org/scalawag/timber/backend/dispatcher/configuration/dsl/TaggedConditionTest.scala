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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Tag
import org.scalawag.timber.backend.dispatcher.EntryFacets

class TaggedConditionTest extends AnyFunSpec with Matchers {

  describe("is") {
    object t1 extends Tag
    object t2 extends Tag
    val c = tagged(t1)

    it("should match when tags contains only the tag") {
      c.accepts(EntryFacets(tags = Some(Set[Tag](t1)))) shouldBe Some(true)
    }

    it("should match when tags contains multiple tags including the tag") {
      c.accepts(EntryFacets(tags = Some(Set[Tag](t1, t2)))) shouldBe Some(true)
    }

    it("should not match when tags is empty") {
      c.accepts(EntryFacets(tags = Some(Set.empty[Tag]))) shouldBe Some(false)
    }

    it("should not match when tags does not contain tag") {
      c.accepts(EntryFacets(tags = Some(Set[Tag](t2)))) shouldBe Some(false)
    }

    it("should abstain when tags is absent") {
      c.accepts(EntryFacets.Empty) shouldBe None
    }

    it("should have the right string representation") {
      c.toString shouldBe s"tagged($t1)"
    }
  }

}
