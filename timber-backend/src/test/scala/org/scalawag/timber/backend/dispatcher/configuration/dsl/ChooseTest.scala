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

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.dispatcher.configuration.debug.DotDumper
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.receiver.Receiver

class ChooseTest extends AnyFunSpec with MockFactory with Matchers {
  val ra = mock[Receiver]
  val rb = mock[Receiver]
  val rc = mock[Receiver]
  val e1 = Entry(level = Some(1))
  val e2 = Entry(level = Some(2))
  val e3 = Entry(level = Some(3))

  it("should be a dead end with no cases") {
    val cfg = Configuration {
      choose()
    }

    cfg.findReceivers(e1) shouldBe Set.empty
    cfg.findReceivers(e2) shouldBe Set.empty
    cfg.findReceivers(e3) shouldBe Set.empty
  }

  it("should pass all entries to the otherwise chain") {
    val cfg = Configuration {
      choose (
        otherwise ~> ra
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(ra)
    cfg.findReceivers(e3) shouldBe Set(ra)
  }

  it("should pass entries to first matching when or drop") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> ra
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set.empty
    cfg.findReceivers(e3) shouldBe Set.empty
  }

  it("should pass entries to first matching when or otherwise") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> ra,
        otherwise ~> rb
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(rb)
    cfg.findReceivers(e3) shouldBe Set(rb)
  }

  it("should pass entries to first matching when of many or drop") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> ra,
        when(level < 3) ~> rb
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(rb)
    cfg.findReceivers(e3) shouldBe Set.empty
  }

  it("should pass entries to first matching when of many or otherwise") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> ra,
        when(level < 3) ~> rb,
        otherwise ~> rc
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(rb)
    cfg.findReceivers(e3) shouldBe Set(rc)
  }

  it("should support chains on when or otherwise cases") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> true ~> ra,
        when(level < 3) ~> true ~> rb,
        otherwise ~> true ~> rc
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(rb)
    cfg.findReceivers(e3) shouldBe Set(rc)
  }

  it("should support long chains on when or otherwise cases") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> true ~> true ~> ra,
        when(level < 3) ~> true ~> true ~> rb,
        otherwise ~> true ~> true ~> rc
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(rb)
    cfg.findReceivers(e3) shouldBe Set(rc)
  }

  it("should allow all cases of the choose to be fanned in") {
    val cfg = Configuration {
      choose (
        when(level < 2) ~> true,
        when(level < 3) ~> true,
        otherwise ~> true
      ) ~> ra
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set(ra)
    cfg.findReceivers(e3) shouldBe Set(ra)
  }

  it("should represent a WhenThen as a String") {
    // This isn't really so much a test as it covers the toString method so it's easy to tell when something important
    // is not covered.
    val wt = when(level < 2) ~> stdout

    wt.toString should fullyMatch regex ".*when.*level.*<.*2.*Console\\.out.*"
  }

  it("should represent a WhenThenWithOutputs as a String") {
    // This isn't really so much a test as it covers the toString method so it's easy to tell when something important
    // is not covered.
    val wt = when(level < 2) ~> true

    wt.toString should fullyMatch regex ".*when.*level.*<.*2.*true.*"
  }
}
