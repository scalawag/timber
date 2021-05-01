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

package org.scalawag.timber.backend.dispatcher.configuration.debug

import java.io.File

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.Receiver

class DotDumperTest extends AnyFunSpec with MockFactory {
  it("should dump a configuration graph to a dot file") {
    val cfg = Configuration {
      true ~> fanout(
        (level < 5) ~> true ~> true,
        (loggingClass startsWith "org.scalawag")
      ) ~> (level > 0) ~> stdout
    }

    DotDumper.dump(cfg, new File("/tmp/test.dot"))
  }
}
