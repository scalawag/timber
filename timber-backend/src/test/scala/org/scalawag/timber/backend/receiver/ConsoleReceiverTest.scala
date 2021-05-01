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

package org.scalawag.timber.backend.receiver

import java.io.{StringWriter, OutputStream, ByteArrayOutputStream}

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.formatter.EntryFormatter

class ConsoleReceiverTest extends AnyFunSpec with Matchers with MockFactory {
  private val messageOnlyFormatter = new EntryFormatter {
    override def format(entry: Entry) = entry.message.get.text
  }

  def entry(msg: String) = new Entry(message = Some(msg))

  describe("OutputStreamReceiver") {

    it("should flush the OutputStream when flushed") {
      val out = mock[OutputStream]("out")
      val r = new ConsoleReceiver(messageOnlyFormatter) {
        override protected def stream = out
      }

      (out.flush _).expects().once

      r.flush()
    }

    it("should flush the OutputStream when closed") {
      val out = mock[OutputStream]("out")
      val r = new ConsoleReceiver(messageOnlyFormatter) {
        override protected def stream = out
      }

      (out.flush _).expects().once

      r.close()
    }

    it("should write to the OutputStream on receive()") {
      val msg = "blah"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleReceiver(messageOnlyFormatter) {
        override protected def stream = baos
      }

      r.receive(entry(msg))
      r.flush()

      new String(baos.toByteArray) shouldBe msg
    }

    it("should allow a non-default character encoding for an OutputStream") {
      val msg = "一二三"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleReceiver(messageOnlyFormatter, Some("GB2312")) {
        override protected def stream = baos
      }

      r.receive(entry(msg))
      r.flush()

      new String(baos.toByteArray, "GB2312") shouldBe msg
    }

  }

  describe("StdoutReceiver") {

    it("should output to Console.out") {
      val msg = "foobar"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleOutReceiver(messageOnlyFormatter)

      Console.withOut(baos) {
        r.receive(entry(msg))
        r.flush()
      }

      new String(baos.toByteArray) shouldBe msg
    }

    it("should not allow you to close Console.out ") {
      val msg = "foobar"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleOutReceiver(messageOnlyFormatter)

      Console.withOut(baos) {
        r.receive(entry(msg))
        r.close()
        r.receive(entry(msg))
        r.close()
      }

      new String(baos.toByteArray) shouldBe (msg + msg)
    }

  }

  describe("ConsoleErrReceiver") {

    it("should output to Console.err") {
      val msg = "foobar"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleErrReceiver(messageOnlyFormatter)

      Console.withErr(baos) {
        r.receive(entry(msg))
        r.flush()
      }

      new String(baos.toByteArray) shouldBe msg
    }

    it("should not allow you to close Console.err ") {
      val msg = "foobar"
      val baos = new ByteArrayOutputStream
      val r = new ConsoleErrReceiver(messageOnlyFormatter)

      Console.withErr(baos) {
        r.receive(entry(msg))
        r.close()
        r.receive(entry(msg))
        r.close()
      }

      new String(baos.toByteArray) shouldBe (msg + msg)
    }

  }
}
