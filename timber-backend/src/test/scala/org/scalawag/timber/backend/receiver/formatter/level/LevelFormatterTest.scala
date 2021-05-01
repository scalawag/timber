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

package org.scalawag.timber.backend.receiver.formatter.level

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Level

class LevelFormatterTest extends AnyFunSpec with Matchers {

  describe("NameLevelFormatter") {

    it("should use the name embedded in the Level") {
      NameLevelFormatter.format(Level(24, "blah")) shouldBe "blah"
    }

    it("should use the level number if there's no name") {
      NameLevelFormatter.format(Level(24)) shouldBe "24"
    }

  }

  describe("NumberLevelFormatter") {

    it("should use the level number even if there's a name") {
      NumberLevelFormatter.format(Level(24, "blah")) shouldBe "24"
    }

    it("should use the level number if there's no name") {
      NumberLevelFormatter.format(Level(24)) shouldBe "24"
    }

  }

  describe("TranslatingLevelFormatter") {
    val f = new TranslatingLevelFormatter(Iterable(Level(5, "foo"), Level(10, "bar"), Level(15, "baz")))

    // Run the same tests for levels with names and levels without.  The difference shouldn't matter to this formatter.

    Iterable(Some("blah"), None) foreach { ln =>
      describe(s"on levels ${if (ln.isDefined) "with" else "without"} names") {

        it("should use the lowest level name for levels below the lowest threshold") {
          f.format(Level(4, ln)) shouldBe "foo"
        }

        it("should use the threshold name at the threshold") {
          f.format(Level(5, ln)) shouldBe "foo"
        }

        it("should use the lower threshold name between thresholds") {
          f.format(Level(11, ln)) shouldBe "bar"
        }

        it("should use the highest level name above the highest threshold") {
          f.format(Level(16)) shouldBe "baz"
        }

      }

    }
  }
}
