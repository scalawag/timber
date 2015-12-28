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

package org.scalawag.timber.api

import scala.language.postfixOps

import org.scalatest.{Matchers, FunSpec}
import java.io.{StringReader, BufferedReader, PrintWriter}

class MessageTest extends FunSpec with Matchers {

  // A function that does nothing except check that the implicit conversion to Message works in method calls.
  private def convert(m:Message):Message = m

  describe("implicit conversions") {

    it("should convert from a String") {
      val msg = convert("blah")
      msg.text should include ("blah")
    }

    it("should convert from a Throwable") {
      val msg = convert(new Throwable("blah"))
      msg.lines.head should include ("blah")
      msg.lines.size shouldBe >= (1)
      msg.text should include (this.getClass.getName)
    }

    it("should convert from a String and a Throwable") {
      val msg = convert("foo",new Exception("bar"))
      msg.lines.head should include ("foo")
      msg.lines.tail.head should include ("bar")
      msg.lines.size shouldBe >= (2)
      msg.text should include (this.getClass.getName)
    }

    it("should convert from a gatherer function") {
      val msg = convert { pw:PrintWriter =>
        pw.print("blah")
      }

      msg.text shouldBe "blah"
    }
  }

  // Given a string that contains newlines, we need code that breaks it into an Iterable[String] where each String
  // contains a single line from the original.
  //
  // I assumed that regexps would outperform using a BufferedReader or Source to split the lines, but I ran some
  // timings and it was faster to use the BufferedReader.  There are unit tests to try and make sure these results
  // continue to hold true in the future, regardless of dependency and JVM version updates.

  describe("getLinesWithSource performance") {
    // create a message with multiple lines
    val text = convert { pw: PrintWriter =>
      pw.println("fake message\nanother line\r\nyet another line\rone with only carriage return")
      (new Exception("not really bad")).printStackTrace(pw)
    } text

    // This is the method used in the code
    def getLinesWithSource(text:String) = scala.io.Source.fromString(text).getLines.toIterable

    // This is an alternate algorithm
    def getLinesWithBufferedReader(text: String) = {
      val br = new BufferedReader(new StringReader(text))
      val lines = scala.collection.mutable.Buffer[String]()
      var line = br.readLine
      while ( line != null ) {
        lines += line
        line = br.readLine
      }
      lines
    }

    // This is another alternate algorithm
    val lineBreakRegex = "(\\n|\\r(\\n)?)".r
    def getLinesWithRegex(text: String) = lineBreakRegex.split(text).toIterable

    // If any of these tests fail, we need to reconsider the algorithm used in Message.getLines

    it("should return the same thing as getLinesWithBufferedReader") {
      getLinesWithSource(text) shouldBe getLinesWithBufferedReader(text)
    }

    it("should be faster than getLinesWithBufferedReader") {
      val iterations = 10000
      val withBufferedReaderTime = time(getLinesWithBufferedReader(text),iterations)
      val withSourceTime = time(getLinesWithSource(text),iterations)

      withSourceTime should be <= (withBufferedReaderTime)
    }

    it("should return the same thing as getLinesWithRegex") {
      getLinesWithSource(text) shouldBe getLinesWithRegex(text)
    }

    it("should be faster than getLinesWithRegex") {
      val iterations = 10000
      val withRegexTime = time(getLinesWithRegex(text),iterations)
      val withSourceTime = time(getLinesWithSource(text),iterations)

      withSourceTime should be <= (withRegexTime)
    }

    def time(fn: => Unit,iterations:Int):Long = {
      val start = System.currentTimeMillis
      (0 until iterations).foreach( _ => fn )
      System.currentTimeMillis - start
    }
  }
}

