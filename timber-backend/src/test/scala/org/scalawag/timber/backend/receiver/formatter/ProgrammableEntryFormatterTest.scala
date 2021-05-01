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

package org.scalawag.timber.backend.receiver.formatter

import java.util.{TimeZone, GregorianCalendar}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.{Level, Entry, Tag}
import org.scalawag.timber.backend.receiver.formatter.level.NumberLevelFormatter
import org.scalawag.timber.backend.receiver.formatter.timestamp.HumanReadableTimestampFormatter
import ProgrammableEntryFormatter._

class ProgrammableEntryFormatterTest extends AnyFunSpec with Matchers {
  private val UTC = TimeZone.getTimeZone("UTC")

  private val time = 72373200123L

  // The thread name will appear in all the output strings produced

  private val threadName = "test"

  describe("headerComponents") {
    val e = Entry(timestamp = time,threadName = threadName)

    it("should use the default formatter (toString)") {
      val f = new ProgrammableEntryFormatter(Seq(entry.timestamp))
      f.format(e) shouldBe "+72373200123\n"
    }

    it("should use the specified timestampFormatter") {
      val df = HumanReadableTimestampFormatter(UTC)
      val f = new ProgrammableEntryFormatter(Seq(entry.timestamp formattedWith df))
      val expectedTimestamp = df.format(e.timestamp)
      f.format(e) shouldBe s"+$expectedTimestamp\n"
    }

  }

  describe("delimiter") {
    val e = Entry(timestamp = time,threadName = threadName)

    it("should use the default delimiter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.timestamp,entry.threadName))
      f.format(e) shouldBe "+72373200123|test\n"
    }

    it("should use the specified delimiter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.timestamp,entry.threadName),delimiter = "&&")
      f.format(e) shouldBe "+72373200123&&test\n"
    }

  }

  describe("continuationHeader") {
    val e = Entry(timestamp = time,threadName = threadName,message=Some("A\nB"))

    it("should use the default (NONE)") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName))
      f.format(e) shouldBe "+test|A\n B\n"
    }

    it("should use NONE as specified explicitly") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName),continuationHeader = ContinuationHeader.NONE)
      f.format(e) shouldBe "+test|A\n B\n"
    }

    it("should use METADATA as specified") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName),continuationHeader = ContinuationHeader.METADATA)
      f.format(e) shouldBe "+test|A\n test|B\n"
    }

    it("should use INDENT as specified") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName),continuationHeader = ContinuationHeader.INDENT)
      f.format(e) shouldBe "+test|A\n      B\n"
    }
  }

  describe("line prefixes") {
    val e = Entry(timestamp = time,threadName = threadName,message=Some("A\nB"))

    it("should use the default prefixes") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName))
      f.format(e) shouldBe "+test|A\n B\n"
    }

    it("should use the specified prefixes") {
      val f = new ProgrammableEntryFormatter(Seq(entry.threadName),firstLinePrefix = "$ ",continuationPrefix = "> ")
      f.format(e) shouldBe "$ test|A\n> B\n"
    }
  }

  describe("entry.level") {
    val e = Entry(timestamp = time,threadName = threadName,level = Some(Level(29,"blah")))

    it("should use the default level formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.level))
      f.format(e) shouldBe "+blah\n"
    }

    it("should use the specified timestampFormatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.level formattedWith NumberLevelFormatter))
      f.format(e) shouldBe "+29\n"
    }
  }

  describe("entry.loggingClass") {

    it("should extract the logging class") {
      val e = Entry(timestamp = time,threadName = threadName,loggingClass = Some("TestCase"))
      val f = new ProgrammableEntryFormatter(Seq(entry.loggingClass))
      f.format(e) shouldBe "+TestCase\n"
    }

    it("should not extract the missing logging class") {
      val e = Entry(timestamp = time,threadName = threadName)
      val f = new ProgrammableEntryFormatter(Seq(entry.loggingClass))
      f.format(e) shouldBe "+\n"
    }

  }

  describe("entry.loggingMethod") {

    it("should extract the logging method") {
      val e = Entry(timestamp = time,threadName = threadName,loggingMethod = Some("doSomething"))
      val f = new ProgrammableEntryFormatter(Seq(entry.loggingMethod))
      f.format(e) shouldBe "+doSomething\n"
    }

    it("should not extract the missing logging method") {
      val e = Entry(timestamp = time,threadName = threadName)
      val f = new ProgrammableEntryFormatter(Seq(entry.loggingMethod))
      f.format(e) shouldBe "+\n"
    }

  }

  describe("entry.tags") {
    object TagA extends Tag {
      override def toString = "TagA"
    }

    object TagB extends Tag {
      override def toString = "TagB"
    }

    val e = Entry(tags = Set(TagA,TagB))

    it("should use the default formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.tags))
      f.format(e) shouldBe "+Set(TagA, TagB)\n"
    }

    it("should use the Commas formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.tags formattedWith Commas))
      f.format(e) shouldBe "+TagA,TagB\n"
    }

    it("should use the Spaces formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.tags formattedWith Spaces))
      f.format(e) shouldBe "+TagA TagB\n"
    }

    it("should use the DelimiterSeparated formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.tags formattedWith Delimiter("%")))
      f.format(e) shouldBe "+TagA%TagB\n"
    }
  }

  describe("entry.loggerAttributes") {
    val e = Entry(loggerAttributes = Map("a" -> 1,"b" -> 2,"c" -> 3))

    it("should use the default formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.loggerAttributes))
      f.format(e) shouldBe "+Map(a -> 1, b -> 2, c -> 3)\n"
    }

    it("should use the CommasAndEquals formatter") {
      val f = new ProgrammableEntryFormatter(Seq(entry.loggerAttributes formattedWith CommasAndEquals))
      f.format(e) shouldBe "+a=1,b=2,c=3\n"
    }
  }

  describe("entry.loggerAttribute") {
    val e = Entry(loggerAttributes = Map("a" -> 1,"b" -> 4))

    it("should extract the right thing") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggerAttribute("a")
      ))
      f.format(e) shouldBe "+1\n"
    }

  }

  describe("entry.threadAttribute") {
    val e = Entry(threadAttributes = Map("a" -> List("1","2","3"),"b" -> List("4","5","6"),"c" -> List()))

    it("should extract the right thing from stack") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.threadAttribute("a")
      ))
      f.format(e) shouldBe "+1\n"
    }

    it("should extract the right thing from empty stack") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.threadAttribute("c")
      ))
      f.format(e) shouldBe "+\n"
    }
  }

  describe("orElse optional") {
    val f = new ProgrammableEntryFormatter(Seq(
      entry.loggingClass orElse entry.loggerAttribute("name") orElse entry.sourceLocation
    ))

    it("should use the preferred header if present") {
      val e = Entry(loggingClass = Some("UnitTest"),loggerAttributes = Map("name" -> "Fallback"))
      f.format(e) shouldBe "+UnitTest\n"
    }

    it("should use the fallback header if the preferred header is absent") {
      val e = Entry(loggerAttributes = Map("name" -> "Fallback"))
      f.format(e) shouldBe "+Fallback\n"
    }

    it("should use the second fallback header if the others are absent") {
      val e = Entry(sourceLocation = Some(Entry.SourceLocation("Test.scala",12)))
      f.format(e) shouldBe "+Test.scala:12\n"
    }

    it("should use the missingValueStr if none of the headers are present") {
      val e = Entry()
      f.format(e) shouldBe "+\n"
    }
  }

  describe("orElse required") {
    val f = new ProgrammableEntryFormatter(Seq(
      entry.loggingClass orElse entry.threadName
    ))

    it("should use the preferred header if present") {
      val e = Entry(loggingClass = Some("UnitTest"),threadName = "THREAD")
      f.format(e) shouldBe "+UnitTest\n"
    }

    it("should use the fallback if the preferred is absent") {
      val e = Entry(threadName = "THREAD")
      f.format(e) shouldBe "+THREAD\n"
    }
  }

  describe("orElse required after orElse optional") {
    val f = new ProgrammableEntryFormatter(Seq(
      entry.loggingClass orElse entry.loggerAttribute("name") orElse entry.threadName
    ))

    it("should use the preferred header if present") {
      val e = Entry(loggingClass = Some("UnitTest"),loggerAttributes = Map("name" -> "Fallback"),threadName = "THREAD")
      f.format(e) shouldBe "+UnitTest\n"
    }

    it("should use the fallback if the preferred is absent") {
      val e = Entry(loggerAttributes = Map("name" -> "Fallback"))
      f.format(e) shouldBe "+Fallback\n"
    }

    it("should use the second fallback header if the others are absent") {
      val e = Entry(threadName = "THREAD")
      f.format(e) shouldBe "+THREAD\n"
    }
  }

  describe("without") {
    val e = Entry(loggerAttributes = Map("a" -> 1,"b" -> 2,"c" -> 3))

    it("should drop a key with without(String)") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggerAttributes without "b" formattedWith CommasAndEquals
      ))

      f.format(e) shouldBe "+a=1,c=3\n"
    }

    it("should drop a key with without(Set[String])") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggerAttributes without Set("b","c") formattedWith CommasAndEquals
      ))

      f.format(e) shouldBe "+a=1\n"
    }

    it("should drop multiple keys with multiple without(String) calls") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggerAttributes without "b" without "c" formattedWith CommasAndEquals
      ))

      f.format(e) shouldBe "+a=1\n"
    }
  }

  describe("map") {
    val e = Entry(loggerAttributes = Map("a" -> 1,"b" -> 2,"c" -> 3),loggingClass = Some("UnitTest"),threadName = "THREAD")

    it("should map a Map") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggerAttributes map { m:Map[String,Any] => m.mapValues( n => s"$n$n" ).toMap } formattedWith CommasAndEquals
      ))

      f.format(e) shouldBe "+a=11,b=22,c=33\n"
    }

    it("should map an optional header") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.loggingClass map (_.reverse)
      ))

      f.format(e) shouldBe "+tseTtinU\n"
    }

    it("should map a required header") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.threadName map (_.reverse)
      ))

      f.format(e) shouldBe "+DAERHT\n"
    }
  }

  describe("TopsOnly") {
    val e = Entry(threadAttributes = Map("a" -> List("1","2","3"),"b" -> List("4","5","6"),"c" -> List()))

    it("should map thread attributes to only the top of the stack") {
      val f = new ProgrammableEntryFormatter(Seq(
        entry.threadAttributes map TopsOnly formattedWith CommasAndEquals
      ))

      f.format(e) shouldBe "+a=1,b=4\n"
    }
  }
}
