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

package test

import java.io.ByteArrayOutputStream

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.BaseLogger

import scala.io.Source

class DebugModeTest extends AnyFunSpec with Matchers {
  it("should throw on logging an entry") {
    val outs = new ByteArrayOutputStream
    val errs = new ByteArrayOutputStream

    Console.withOut(outs) {
      Console.withErr(errs) {
        (new BaseLogger).log(0)("blah")
      }
    }

    val out = Source.fromBytes(outs.toByteArray).getLines.toSeq
    val err = Source.fromBytes(errs.toByteArray).getLines.toSeq

    out.length shouldBe 0
    err.length shouldBe 2

    err(0) should fullyMatch regex ".*DEBUG.*timber.*default dispatcher.*"
    err(1) should fullyMatch regex "\\+.*blah"
  }
}
