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
import org.scalatest.{FunSpec, Matchers}
import org.scalawag.timber.api.{Level, Entry}
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.debug.DotDumper
import org.scalawag.timber.backend.receiver.Receiver

class FanoutTest extends FunSpec with MockFactory with Matchers {
  val ra = mock[Receiver]
  val rb = mock[Receiver]
  val rc = mock[Receiver]
  val e1 = Entry(level = Some(Level.DEBUG))
  val e2 = Entry(level = Some(Level.INFO))
  val e3 = Entry(level = Some(Level.WARN))

  it("should be a dead end with no chains") {
    val cfg = Configuration {
      fanout()
    }

    cfg.findReceivers(e1) shouldBe Set.empty
  }

  it("should pass all entries to a single chain") {
    val cfg = Configuration {
      fanout(ra)
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
  }

  it("should pass all entries to all chains") {
    val cfg = Configuration {
      fanout(ra,rb,rc)
    }

    cfg.findReceivers(e1) shouldBe Set(ra,rb,rc)
  }

  it("should support chains as arguments") {
    val cfg = Configuration {
      fanout (
        true ~> ra,
        true ~> rb,
        true ~> rc
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra,rb,rc)
  }

  it("should support long chains as arguments") {
    val cfg = Configuration {
      fanout (
        true ~> true ~> ra,
        true ~> true ~> rb,
        true ~> true ~> rc
      )
    }

    cfg.findReceivers(e1) shouldBe Set(ra,rb,rc)
  }

  it("should allow all chains to be fanned back in") {
    val cfg = Configuration {
      fanout (
        ( level < Level.INFO ) ~> true,
        ( level > Level.INFO ) ~> true
      ) ~> ra
    }

    cfg.findReceivers(e1) shouldBe Set(ra)
    cfg.findReceivers(e2) shouldBe Set.empty
    cfg.findReceivers(e3) shouldBe Set(ra)
  }
}
