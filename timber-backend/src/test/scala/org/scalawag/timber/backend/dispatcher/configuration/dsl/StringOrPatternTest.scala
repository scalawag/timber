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

import java.util.regex.Pattern

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class StringOrPatternTest extends AnyFunSpec with Matchers {
  describe("implicit conversions") {

    it("should convert from a String") {
      val sop:StringOrPattern = "literal"
      sop shouldBe StringOrPattern(Left("literal"))
    }

    it("should convert from a Scala Regex") {
      val sop:StringOrPattern = "literal".r
      sop shouldBe StringOrPattern(Right(Pattern.compile("literal")))
    }

    it("should convert from a Java Pattern") {
      val sop:StringOrPattern = Pattern.compile("literal")
      sop shouldBe StringOrPattern(Right(Pattern.compile("literal")))
    }

  }

  describe("string") {
    declareTests(StringOrPattern(Left("a")))
  }

  describe("pattern") {
    declareTests(StringOrPattern(Right(Pattern.compile("[Aa]"))))
  }

  private def declareTests(sop:StringOrPattern): Unit = {
    describe("matches") {

      it("should match full string") {
        sop.matches("a") shouldBe true
      }

      it("should not match partial string at start") {
        sop.matches("ab") shouldBe false
      }

      it("should not match partial string at end") {
        sop.matches("ba") shouldBe false
      }
  
    }

    describe("isContainedIn") {
  
      it("should match full string") {
        sop.isContainedIn("a") shouldBe true
      }

      it("should match partial string at start") {
        sop.isContainedIn("ab") shouldBe true
      }

      it("should match partial string at end") {
        sop.isContainedIn("ba") shouldBe true
      }
  
    }
  
    describe("starts") {
  
      it("should match full string") {
        sop.starts("a") shouldBe true
      }

      it("should match partial string at start") {
        sop.starts("ab") shouldBe true
      }

      it("should not match partial string at end") {
        sop.starts("ba") shouldBe false
      }
  
    }

    describe("ends") {

      it("should match full string") {
        sop.ends("a") shouldBe true
      }

      it("should not match partial string at start") {
        sop.ends("ab") shouldBe false
      }

      it("should match partial string at end") {
        sop.ends("ba") shouldBe true
      }

    }

  }

}
