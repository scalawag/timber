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

package org.scalawag.timber.test

import java.io.{ByteArrayOutputStream, BufferedWriter, StringWriter}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, FunSpec}
import org.scalawag.timber.api.{Entry, BaseLogger}
import org.scalawag.timber.backend.InternalLogging
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.receiver.{Receiver, WriterBasedReceiver}
import sun.misc.Signal

class CloseOnSignalTest extends FunSpec with Matchers with MockFactory with Eventually {

  it("should close specified Receivers on SIGHUP") {
    import org.scalawag.timber.backend.dispatcher.configuration.dsl._

    val sw1 = new StringWriter
    val sw2 = new StringWriter
    val sw3 = new StringWriter
    val sw4 = new StringWriter

    val r1 = new WriterBasedReceiver(new BufferedWriter(sw1))
    val r2 = new WriterBasedReceiver(new BufferedWriter(sw2))
    val r3 = new WriterBasedReceiver(new BufferedWriter(sw3))
    val r4 = new WriterBasedReceiver(new BufferedWriter(sw4))

    Receiver.closeOnSignal("HUP",r2)
    Receiver.closeOnSignal("HUP",r3,r4)

    val cfg = Configuration {
      true ~> fanout(r1,r2,r3,r4)
    }

    implicit val dispatcher = new Dispatcher(cfg)
    val logger = new BaseLogger
    logger.log(0)("blah")
    logger.log(1)("blah")

    // All output should be buffered for now...

    sw1.toString shouldBe 'empty
    sw2.toString shouldBe 'empty
    sw3.toString shouldBe 'empty
    sw4.toString shouldBe 'empty

    Signal.raise(new Signal("HUP"))

    // All the receivers registered to close on the signal should have been flushed now.

    eventually {
      sw1.toString shouldBe 'empty
      sw2.toString should not be 'empty
      sw3.toString should not be 'empty
      sw4.toString should not be 'empty
    }
  }

  it("should complain if a Receiver can't be closed") {
    val r = new Receiver {
      override def receive(entry: Entry) = ???
      override def flush() = ???
      override def close() = throw new Exception("boom")
    }

    val internalLogging = new ByteArrayOutputStream
    InternalLogging.outputStreamOverride = Some(internalLogging)

    Receiver.closeOnSignal("HUP",r)
    Signal.raise(new Signal("HUP"))

    eventually {
      val output = new String(internalLogging.toByteArray)
      output should include ("boom")
    }

    InternalLogging.outputStreamOverride = None
  }
}
