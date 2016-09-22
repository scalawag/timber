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

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FunSpec}
import org.scalawag.timber.api.{Entry, BaseLogger}
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.Receiver

class CloseOnShutdownTest extends FunSpec with Matchers with MockFactory {
  // TODO: make this fully automated

  // Not actually a test right now.  There are no assertions.  You have to run it and then look to see that
  // /tmp/b contains messages (because it's set up to be closed on shutdown) and /tmp/a does not (because it's
  // not set up that way.  It still gives us test coverage and proves that the code doesn't fail spectacularly.

  it("should close specified Receivers on shutdown") {
    val r1 = file("/tmp/a")
    val r2 = file("/tmp/b")
    val r3 = file("/tmp/c")

    Receiver.closeOnShutdown(r2)
    val cfg = Configuration {
      true ~> fanout(r1,r2,r3)
    }

    implicit val dispatcher = new Dispatcher(cfg)
    val logger = new BaseLogger

    logger.log(0)("blah")
    logger.log(1)("blah")
  }

  it("should complain if a specified Receiver can't be closed") {
    val r = new Receiver {
      override def receive(entry: Entry) = ???
      override def flush() = ???
      override def close() = ???
    }
    Receiver.closeOnShutdown(r)
  }
}
