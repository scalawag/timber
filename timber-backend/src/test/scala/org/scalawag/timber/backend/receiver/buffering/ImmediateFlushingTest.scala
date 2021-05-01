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

package org.scalawag.timber.backend.receiver.buffering

import java.io.{StringWriter, Writer, PrintWriter}

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.WriterBasedStackableReceiver
import org.scalawag.timber.backend.receiver.formatter.DefaultEntryFormatter

class ImmediateFlushingTest extends AnyFunSpec with MockFactory {
  private class MockablePrintWriter extends PrintWriter(new StringWriter)
  private val pw: PrintWriter = mock[MockablePrintWriter]
  private val oneLineEntry = new Entry(message = Some("foo"))
  private val twoLineEntry = new Entry(message = Some("foo\nbar"))

  it("should not flush on receive without ImmediateFlushing") {
    (pw.write(_: String)).expects(*).once

    val receiver = new WriterBasedStackableReceiver(pw)
    receiver.receive(oneLineEntry)
  }

  it("should flush on receive with ImmediateFlushing") {
    inSequence {
      (pw.write(_: String)).expects(*).once
      (pw.flush _).expects().once
    }

    val receiver = new WriterBasedStackableReceiver(pw).flushImmediately
    receiver.receive(oneLineEntry)
  }

  it("should flush once on multi-line receive with ImmediateFlushing") {
    inSequence {
      (pw.write(_: String)).expects(*).once
      (pw.flush _).expects().once
    }

    val receiver = ImmediateFlushing(new WriterBasedStackableReceiver(pw))
    receiver.receive(twoLineEntry)
  }

  it("should flush once per receive with ImmediateFlushing") {
    inSequence {
      (pw.write(_: String)).expects(*).once
      (pw.flush _).expects().once
      (pw.write(_: String)).expects(*).once
      (pw.flush _).expects().once
      (pw.write(_: String)).expects(*).once
      (pw.flush _).expects().once
    }

    val receiver = ImmediateFlushing(new WriterBasedStackableReceiver(pw))
    receiver.receive(oneLineEntry)
    receiver.receive(twoLineEntry)
    receiver.receive(oneLineEntry)
  }
}
