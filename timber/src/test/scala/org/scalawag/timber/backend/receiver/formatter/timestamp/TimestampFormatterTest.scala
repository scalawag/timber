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

package org.scalawag.timber.backend.receiver.formatter.timestamp

import java.util.TimeZone

import org.scalatest.{FunSpec, Matchers}

class TimestampFormatterTest extends FunSpec with Matchers {
  private val time = 72373200123L

  it("should format properly with ISO8601TimestampFormatter and default TimeZone") {
    ISO8601TimestampFormatter.format(time) shouldBe ISO8601TimestampFormatter(TimeZone.getDefault).format(time)
  }

  it("should format properly with ISO8601TimestampFormatter and specified TimeZone") {
    ISO8601TimestampFormatter(TimeZone.getTimeZone("UTC")).format(time) shouldBe "1972-04-17T15:40:00.123Z"
  }

  it("should format properly with HumanReadableTimestampFormatter and default TimeZone") {
    HumanReadableTimestampFormatter.format(time) shouldBe HumanReadableTimestampFormatter(TimeZone.getDefault).format(time)
  }

  it("should format properly with HumanReadableTimestampFormatter and specified TimeZone") {
    HumanReadableTimestampFormatter(TimeZone.getTimeZone("EST")).format(time) shouldBe "1972-04-17 10:40:00.123 EST"
  }

  it("should format properly with JavaEpochTimestampFormatter") {
    JavaEpochTimestampFormatter.format(time) shouldBe "72373200123"
  }

  it("should format properly with custom SimpleDateFormatTimestampFormatter") {
    new SimpleDateFormatTimestampFormatter("EEEE, MMMM d, y").format(time) shouldBe "Monday, April 17, 1972"
  }
}
