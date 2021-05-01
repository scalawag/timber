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

package org.scalawag.timber.backend.dispatcher.configuration

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.dispatcher.EntryFacets
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.Receiver

class ImmutableVertexTest extends AnyFunSpec with Matchers with MockFactory {

  describe("ImmutableConditionVertex") {

    it("should represent condition vertex as a string") {
      val c = new Condition {
        override def accepts(entryFacets: EntryFacets) = ???
        override def toString = "blazz"
      }

      val v = new ImmutableConditionVertex(c, Set.empty)

      v.toString shouldBe "blazz"
    }

  }

  describe("ImmutableReceiverVertex") {

    it("should represent receiver vertex as a string") {
      val r = new Receiver {
        override def receive(entry: Entry) = ???
        override def flush() = ???
        override def close() = ???
        override def toString = "farfel"
      }

      val v = new ImmutableReceiverVertex(r)

      v.toString shouldBe "farfel"
    }

  }

  describe("ImmutableVertex") {

    it("should convert a MutableConditionVertex") {
      val c = mock[Condition]
      val mv = new MutableConditionVertex(c)

      val iv = ImmutableVertex(mv)

      iv.asInstanceOf[ImmutableConditionVertex].condition shouldBe c
    }

    it("should convert a MutableReceiverVertex") {
      val r = mock[Receiver]
      val mv = new MutableReceiverVertex(r)

      val iv = ImmutableVertex(mv)

      iv.asInstanceOf[ImmutableReceiverVertex].receiver shouldBe r
    }

    it("should reuse repeated vertices") {
      // To ensure that both times c3 is encountered, the same ImmutableVertex is used

      val b1 = Subgraph(true)
      val c1 = Subgraph(level > 1)
      val c2 = Subgraph(level > 2)
      var r1 = Subgraph(mock[Receiver])

      val g = b1 ~> fanout(c1, c2) ~> r1

      val ie = ImmutableVertex(g.root)

      def findleaves(v: ImmutableVertex): Set[ImmutableVertex] =
        v match {
          case ImmutableConditionVertex(_, nexts) => nexts.flatMap(findleaves)
          case ImmutableReceiverVertex(_)         => Set(v)
        }

      findleaves(ie).size shouldBe 1
    }

  }

}
