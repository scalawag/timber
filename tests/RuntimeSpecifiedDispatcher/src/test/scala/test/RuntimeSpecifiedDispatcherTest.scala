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

package test

import java.io.ByteArrayOutputStream

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.{Dispatcher, Entry, BaseLogger}
import org.scalawag.timber.backend.DefaultDispatcher

import scala.io.Source

object TestMain {
  DefaultDispatcher.set(new ThrowingDispatcher)

  // A dispatcher that throws an Exception on dispatch so that we can identify it easily.

  class ThrowingDispatcher extends Dispatcher {
    override def dispatch(entry: Entry) = throw new Exception("boom")
  }

  def go() = {
    (new BaseLogger).log(0)("blah")
  }
}

class RuntimeSpecifiedDispatcherTest extends AnyFunSpec with Matchers {
  it("should throw on logging an entry") {
    val outs = new ByteArrayOutputStream
    val errs = new ByteArrayOutputStream

    val ex =
      intercept[Exception] {
        Console.withOut(outs) {
          Console.withErr(errs) {
            TestMain.go
          }
        }
      }

    val out = Source.fromBytes(outs.toByteArray).getLines.toSeq
    val err = Source.fromBytes(errs.toByteArray).getLines.toSeq

    out.length shouldBe 0
    err.length shouldBe 0

    ex.getMessage shouldBe "boom"
  }
}
