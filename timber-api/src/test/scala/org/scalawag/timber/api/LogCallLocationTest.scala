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

package org.scalawag.timber.api

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.BaseLogger.LogCallLocation

class LogCallLocationTest extends AnyFunSpec with Matchers {
  def grab(implicit x: LogCallLocation) = x

  it("capture") {
    val loc = grab

    loc.sourceLocation.filename shouldBe "LogCallLocationTest.scala"
    loc.sourceLocation.lineNumber shouldBe 25
    loc.className shouldBe Some(this.getClass.getName)
    loc.methodName shouldBe None
  }

  def generateTestInMethod(): Unit = {
    it("should capture method name") {
      val loc = grab
      loc.methodName shouldBe Some("generateTestInMethod")
    }
  }

  generateTestInMethod()
}
