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

package org.scalawag.timber.backend.receiver

import java.io.Writer
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.formatter.EntryFormatter

// TODO: this needs tests to handle multiple opens (and closes)

class WriterBasedReceiverTest extends AnyFunSpec with Matchers with MockFactory {
  private val writer = mock[Writer]("writer")
  private val createWriterFn = mockFunction[Writer]("createWriterFn")
  private val formatter = mock[EntryFormatter]("formatter")
  private val entry = new Entry()

  def createWriterCalled = createWriterFn.expects().returns(writer)
  def formatterFormatCalled = (formatter.format _).expects(*).onCall { e: Entry => "" }
  def writerWriteCalled = (writer.write(_: String)).expects("")
  def writerFlushCalled = (writer.flush _).expects()
  def writerCloseCalled = (writer.close _).expects()

  it("should create the writer when receive() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once
    }

    wber.receive(entry)
  }

  it("should reuse the writer when receive() is called twice") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once
    }

    wber.receive(entry)
    wber.receive(entry)
  }

  it("should not create the writer when flush() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    wber.flush()
  }

  it("should flush the writer when flush() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerFlushCalled.once
    }

    wber.receive(entry)
    wber.flush()
  }

  it("should not create the writer when close() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    wber.close()
  }

  it("should close the writer when close() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerCloseCalled.once
    }

    wber.receive(entry)
    wber.close()
  }

  it("should close the writer only once when close() is called twice") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerCloseCalled.once
    }

    wber.receive(entry)
    wber.close()
    wber.close()
  }

  it("should recreate the writer when receive() is called after close() is called") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerCloseCalled.once

      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once
    }

    wber.receive(entry)
    wber.close()
    wber.receive(entry)
  }

  it("should bubble write() exceptions out") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.throws(new Exception("gah")).once
    }

    val ex = intercept[Exception] { wber.receive(entry) }
    ex.getMessage shouldBe "gah"

  }

  it("should bubble flush() exceptions out") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerFlushCalled.throws(new Exception("gah")).once
    }

    wber.receive(entry)
    val ex = intercept[Exception] { wber.flush() }
    ex.getMessage shouldBe "gah"

  }

  it("should bubble close() exceptions out") {
    val wber = new WriterBasedReceiver(createWriterFn())(formatter)

    inSequence {
      createWriterCalled.once
      formatterFormatCalled.once
      writerWriteCalled.once

      writerCloseCalled.throws(new Exception("gah")).once
    }

    wber.receive(entry)
    val ex = intercept[Exception] { wber.close() }
    ex.getMessage shouldBe "gah"

  }
}
