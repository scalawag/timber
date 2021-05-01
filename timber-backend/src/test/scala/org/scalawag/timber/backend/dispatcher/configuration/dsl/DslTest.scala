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

package org.scalawag.timber.backend.dispatcher.configuration.dsl

import org.scalactic.Prettifier
import org.scalactic.source.Position
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.{Entry, Level, Message}
import org.scalawag.timber.backend.dispatcher.EntryFacets
import org.scalawag.timber.backend.dispatcher.configuration.dsl
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition.{AcceptAll, RejectAll}
import org.scalawag.timber.backend.receiver.Receiver
import org.scalawag.timber.backend.receiver.formatter.EntryFormatter

import scala.reflect.{ClassTag, classTag}

class DslTest extends AnyFunSpec with Matchers with MockFactory {

  describe("implicit subgraph wrapping") {

    it("should wrap Boolean when adding an edge") {
      val g = true ~> false

      val v1 = g.root.asInstanceOf[MutableConditionVertex]
      val v2 = g.leaves.head.asInstanceOf[MutableConditionVertex]

      v1.condition shouldBe AcceptAll
      v1.nexts shouldBe Set(v2)
      v2.condition shouldBe RejectAll
      v2.nexts shouldBe 'empty
    }

    it("should wrap Condition when adding an edge") {
      val c = mock[Condition]
      val g = c ~> false

      val v1 = g.root.asInstanceOf[MutableConditionVertex]
      val v2 = g.leaves.head

      v1.condition shouldBe c
      v1.nexts shouldBe Set(v2)
      v2.condition shouldBe RejectAll
      v2.nexts shouldBe 'empty
    }

    it("should wrap Receiver when adding an edge") {
      val r = mock[Receiver]
      val g = true ~> r

      val v1 = g.root.asInstanceOf[MutableConditionVertex]
      val v2 = g.leaves.head

      v1.condition shouldBe AcceptAll
      v1.nexts shouldBe Set(v2)
      v2.receiver shouldBe r
    }
  }

  describe("vertex string representations") {

    it("should represent filters") {
      val c = level < 5
      val f = new MutableConditionVertex(c)
      f.toString shouldBe "level < 5"
    }

    it("should represent receivers") {
      val er = new Receiver {
        override def receive(entry: Entry) = ???

        override def flush() = ???

        override def close() = ???

        override val toString = "garp"
      }
      val v = new MutableReceiverVertex(er)
      v.toString shouldBe "garp"
    }
  }

  describe("Receiver wrapping") {

    it("should wrap with a Chain when necessary") {
      val er = mock[Receiver]

      val g: Subgraph[MutableReceiverVertex] = er

      narrow[MutableReceiverVertex](g.root).receiver shouldBe er
      g.leaves.head.receiver shouldBe er
    }

    it("should implicitly wrap when chaining") {
      val er = mock[Receiver]
      val g: Subgraph[MutableReceiverVertex] = true ~> er

      narrow[MutableConditionVertex](g.root).condition shouldBe AcceptAll
      g.leaves.head.receiver shouldBe er
    }

    it("should produce a new Receiver each time an Receiver is wrapped") {
      val er = mock[Receiver]

      val e1: Subgraph[MutableReceiverVertex] = er
      val e2: Subgraph[MutableReceiverVertex] = er

      // You should get different Receiver instances wrapping the same Receiver.

      e1.leaves.head should not be (e2.leaves.head)
      e1.leaves.head.receiver shouldBe e2.leaves.head.receiver
    }

  }

  describe("Condition wrapping") {

    it("should wrap with a Chain when necessary") {
      val cnd = mock[Condition]

      val c: Subgraph[MutableConditionVertex] = cnd

      c.root.asInstanceOf[MutableConditionVertex].condition shouldBe cnd
      c.leaves.head.condition shouldBe cnd
    }

    it("should implicitly wrap when chaining") {
      val cnd = mock[Condition]
      val g: Subgraph[MutableConditionVertex] = true ~> cnd

      narrow[MutableConditionVertex](g.root).condition shouldBe AcceptAll
      g.leaves.head.condition shouldBe cnd
    }

    it("should produce a new Filter each time a Condition is wrapped") {
      val cnd = mock[Condition]

      val e1: Subgraph[MutableConditionVertex] = cnd
      val e2: Subgraph[MutableConditionVertex] = cnd

      // You should get different Receiver instances wrapping the same Receiver.  This way they can have
      // different outputs even though they are both using the same condition.

      e1.leaves.head should not be (e2.leaves.head)
      e1.leaves.head.condition shouldBe e2.leaves.head.condition
    }

  }

  describe("receivers") {
    it("should allow configurable file receivers to be created easily") {
      import scala.concurrent.duration._
      import org.scalawag.timber.backend.receiver.buffering._
      import org.scalawag.timber.backend.receiver.concurrency._

      implicit val formatter = mock[EntryFormatter]

      val ra: Receiver = Queueing(PeriodicFlushing(file("/tmp/a")))
      val rb: Receiver = Locking(PeriodicFlushing(file("/tmp/b"), 1.second))
      val rc: Receiver = ImmediateFlushing(file("/tmp/c"))
      val rd: Receiver = Queueing(file("/tmp/d"))
    }

    it("should console receivers to be created easily") {
      implicit val formatter = mock[EntryFormatter]

      val ra: Receiver = stdout
      val rb: Receiver = stderr
    }
  }

  describe("loggingClass") {
    val scf = loggingClass

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    it("should extract correctly from full EntryFacets without loggingClass") {
      scf.extractFrom(EntryFacets(loggingClass = Some(None))) shouldBe Some(Iterable.empty)
    }

    it("should extract correctly from full EntryFacets with loggingClass") {
      scf.extractFrom(EntryFacets(loggingClass = Some(Some("TimberTest")))) shouldContainSameItemsAs Some(
        Iterable("TimberTest")
      )
    }
  }

  describe("level") {
    val scf = level

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    it("should extract correctly from full EntryFacets without level") {
      scf.extractFrom(EntryFacets(level = Some(None))) shouldBe Some(Iterable.empty)
    }

    it("should extract correctly from full EntryFacets with level") {
      scf.extractFrom(EntryFacets(level = Some(Some(Level(88, "name"))))) shouldContainSameItemsAs Some(Iterable(88))
    }
  }

  describe("message") {
    val scf = dsl.message

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    it("should extract correctly from full EntryFacets without message") {
      scf.extractFrom(EntryFacets(message = Some(None))) shouldBe Some(Iterable.empty)
    }

    it("should extract correctly from full EntryFacets with message") {
      scf.extractFrom(EntryFacets(message = Some(Some("foo bar baz": Message)))) shouldContainSameItemsAs Some(
        Iterable("foo bar baz")
      )
    }
  }

  describe("sourceFile") {
    val scf = sourceFile

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    it("should extract correctly from full EntryFacets without sourceFile") {
      scf.extractFrom(EntryFacets(sourceFile = Some(None))) shouldBe Some(Iterable.empty)
    }

    it("should extract correctly from full EntryFacets with sourceFile") {
      scf.extractFrom(EntryFacets(sourceFile = Some(Some("TimberTest.scala")))) shouldContainSameItemsAs Some(
        Iterable("TimberTest.scala")
      )
    }
  }

  describe("logger(attr)") {
    val scf = logger("name")

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    // TODO: absent case
    it("should extract correctly from EntryFacets without the key") {
      scf.extractFrom(EntryFacets(loggerAttributes = Some(Map.empty[String, Any]))) shouldBe None
    }

    it("should extract correctly from EntryFacets with the key") {
      scf.extractFrom(EntryFacets(loggerAttributes = Some(Map("name" -> "chester")))) shouldBe Some(Iterable("chester"))
    }
  }

  describe("thread(attr).any") {
    val scf = thread("name").any

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }
    // TODO: absent case

    it("should extract correctly from EntryFacets without the key") {
      scf.extractFrom(EntryFacets(threadAttributes = Some(Map.empty[String, List[String]]))) shouldBe None
    }

    it("should extract correctly from EntryFacets with the key") {
      scf.extractFrom(EntryFacets(threadAttributes = Some(Map("name" -> List("sam", "chester"))))) shouldBe Some(
        Iterable("sam", "chester")
      )
    }
  }

  describe("thread(attr).top") {
    val scf = thread("name").top

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    // TODO: absent case

    it("should extract correctly from EntryFacets without the key") {
      scf.extractFrom(EntryFacets(threadAttributes = Some(Map.empty[String, List[String]]))) shouldBe None
    }

    it("should extract correctly from EntryFacets with the key") {
      scf.extractFrom(
        EntryFacets(threadAttributes = Some(Map("name" -> List("sam", "chester"))))
      ) shouldContainSameItemsAs Some(Iterable("sam"))
    }
  }

  describe("thread.name") {
    val scf = thread.name

    it("should extract correctly from empty EntryFacets") {
      scf.extractFrom(EntryFacets.Empty) shouldBe None
    }

    it("should extract correctly from full EntryFacets") {
      scf.extractFrom(EntryFacets(threadName = Some("blech"))) shouldBe Some(Iterable("blech"))
    }
  }

  private def narrow[T: ClassTag](obj: Any) =
    try {
      obj.asInstanceOf[T]
    } catch {
      case c: ClassCastException =>
        fail("object " + obj + " is not of the expected type (" + classTag[T].runtimeClass + ")")
    }

  implicit sealed class AnyOps(l: Option[Iterable[_]]) {
    def shouldContainSameItemsAs(r: Option[Iterable[_]]): Assertion = {
      l.map(_.toList) shouldBe r.map(_.toList)
    }
  }
}
