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

package org.scalawag.timber.api

import java.io.PrintWriter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FunSpec}

class BaseLoggerTest extends FunSpec with Matchers with MockFactory {
  implicit val dispatcher = mock[Dispatcher]

  describe("lazy message evaluation") {
    trait Fixture {
      var evaluated = false
      val logger = new BaseLogger()
      val message = { pw:PrintWriter =>
        evaluated = true
      }

      (dispatcher.dispatch _).expects(*).once
    }

    it("should not evaluate Message until it's necessary") {
      new Fixture {
        logger.log(0)(message)

        evaluated shouldBe false
      }
    }

    it("should evaluate Message immediately with ImmediateMessage tag") {
      new Fixture {
        logger.log(0,Set(ImmediateMessage))(message)

        evaluated shouldBe true
      }
    }

  }

  describe("styles of logging calls") {
    it("should allow multiple tags with a message gatherer block") {
      val logger = new BaseLogger

      (dispatcher.dispatch _).expects(*).once

      logger.log(0,Set(ImmediateMessage)) { pw:PrintWriter =>
        pw.println("blah")
      }
    }

    it("should allow a string and an exception (implicit tuple conversion)") {
      val logger = new Logger

      (dispatcher.dispatch _).expects(*).once

      val ex = new Exception("boom")

      logger.warn("blah",ex)
    }
  }
}
