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

package org.scalawag.timber.backend.dispatcher.configuration

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.backend.receiver.Receiver

class ConfigurationTest extends AnyFunSpec with Matchers with MockFactory with DogmaticConditions {

  describe("constrain") {
    import dsl._

    val r = mock[Receiver]("r")
    val cfg = Configuration

    it("should remove closed valves (and everything below)") {
      cfg(false ~> r).constrain() shouldBe Configuration.empty
    }

    it("should remove closed valves (and everything below (unless otherwise reachable))") {
      val c = cfg(
        ABSTAIN ~> fanout(
          false,
          ABSTAIN
        ) ~> r
      )

      val cc = cfg(
        ABSTAIN ~> ABSTAIN ~> r
      )

      c.constrain() shouldBe cc
    }

    it("should collapse open valves") {
      cfg(true ~> ABSTAIN ~> r).constrain() shouldBe cfg(ABSTAIN ~> r)
    }

    it("should remove filters that block the entry (and everything below)") {
      cfg(FALSE ~> r).constrain() shouldBe Configuration.empty
    }

    it("should remove filters that don't match the entry (and everything below (unless otherwise reachable))") {
      val c = cfg(
        ABSTAIN ~> fanout(
          FALSE,
          ABSTAIN
        ) ~> r
      )

      val cc = cfg(
        ABSTAIN ~> ABSTAIN ~> r
      )

      c.constrain() shouldBe cc
    }

    it("should maintain filters that abstain") {
      cfg(ABSTAIN ~> r).constrain() shouldBe cfg(ABSTAIN ~> r)
    }

    it("should collapse filters that match the entry") {
      cfg(TRUE ~> r).constrain() shouldBe cfg(r)
    }

    it("should remove everything leading up to a dead end (no receiver)") {
      cfg(ABSTAIN ~> ABSTAIN ~> ABSTAIN).constrain() shouldBe Configuration.empty
    }
  }

  describe("findReceivers") {}
}
