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

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.dispatcher.EntryFacets
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition.AcceptAll
import org.scalawag.timber.backend.receiver.Receiver

class MutableVertexTest extends AnyFunSpec with Matchers with MockFactory {

  describe("MutableVertexWithOutputs") {

    it ("should throw an exception at run time on direct cycle") {
      val v = new MutableConditionVertex(AcceptAll)

      an [IllegalArgumentException] shouldBe thrownBy ( v addNext v )
    }

    it("should throw an exception at run-time on indirect cycles") {
      val v1 = new MutableConditionVertex(AcceptAll)
      val v2 = new MutableConditionVertex(AcceptAll)

      v1 addNext v2

      an [IllegalArgumentException] shouldBe thrownBy ( v2 addNext v1 )
    }

    it ("should not affect outputs when a direct cycle is detected") {
      val v1 = new MutableConditionVertex(AcceptAll)

      an [IllegalArgumentException] shouldBe thrownBy ( v1 addNext v1 )

      v1.nexts shouldBe 'empty
    }

    it("should not affect outputs when an indirect cycle is detected") {
      val v1 = new MutableConditionVertex(AcceptAll)
      val v2 = new MutableConditionVertex(AcceptAll)

      v1 addNext v2

      an [IllegalArgumentException] shouldBe thrownBy ( v2 addNext v1 )

      v1.nexts shouldBe Set(v2)
      v2.nexts shouldBe 'empty
    }
  }

  describe("MutableConditionVertex") {

    it("should represent condition vertex as a string") {
      val c = new Condition {
        override def accepts(candidate: EntryFacets) = ???
        override def toString = "blazz"
      }

      val v = new MutableConditionVertex(c)

      v.toString shouldBe "blazz"
    }

  }

  describe("MutableReceiverVertex") {

    it("should represent receiver vertex as a string") {
      val r = new Receiver {
        override def receive(entry: Entry) = ???
        override def flush() = ???
        override def close() = ???
        override def toString = "farfel"
      }

      val v = new MutableReceiverVertex(r)

      v.toString shouldBe "farfel"
    }

  }

  describe("Subgraph") {

    it("should create single-vertex subgraph from Boolean") {
      val g = Subgraph(true)

      val h = g.root.asInstanceOf[MutableConditionVertex]
      h.condition shouldBe AcceptAll
      h.nexts shouldBe 'empty

      g.leaves shouldBe Seq(h)
    }

    it("should create single-vertex subgraph from Condition") {
      val c = mock[Condition]
      val g = Subgraph(c)

      val h = g.root.asInstanceOf[MutableConditionVertex]
      h.condition shouldBe c
      h.nexts shouldBe 'empty

      g.leaves shouldBe Seq(h)
    }

    it("should create single-vertex subgraph from Receiver") {
      val r = mock[Receiver]
      val g = Subgraph(r)

      val h = g.root.asInstanceOf[MutableReceiverVertex]
      h.receiver shouldBe r

      g.leaves shouldBe Seq(h)
    }

    it("should add another subgraph after leaves") {
      val g1 = Subgraph(true)
      val g2 = Subgraph(false)

      val g3 = g1 ~> g2

      g3.root shouldBe g1.root
      g3.leaves shouldBe g2.leaves
    }

    it("should add another terminal subgraph after leaves") {
      val r = mock[Receiver]
      val g1 = Subgraph(true)
      val g2 = Subgraph(r)

      val g3 = g1 ~> g2

      g3.root shouldBe g1.root
      g3.leaves.toSet shouldBe Set(g2.root)
    }

    // A Subgraph represents a partial view of the entire graph based on what was just built.  This means that, if
    // you took the root and followed all the nexts of the vertices, you could get a different set than if you just
    // looked at the leaves of the Subgraph.  This seems kind of counterintuitive at first, but it makes sense given
    // the fact that this is part of a DSL.  When you build a Subgraph using the DSL, you can add an edge to it and
    // the only vertices affected are those that were specifically mentioned when building the subgraph.  Otherwise,
    // you'd be creating edges that you weren't expecting.

    it("should have leaves independent of its containing graph") {

      val b1 = Subgraph(true)
      val c1 = Subgraph(level > 1)
      val c2 = Subgraph(level > 2)
      val c3 = Subgraph(level > 3)
      val c4 = Subgraph(level > 4)
      val c5 = Subgraph(level > 5)

      val g1 = b1 ~> c1 ~> c2 ~> c3

      g1.root shouldBe b1.root
      g1.leaves shouldBe c3.leaves

      val g2 = b1 ~> c4 ~> c2 ~> c5

      g2.root shouldBe b1.root
      g2.leaves shouldBe c5.leaves // shouldn't reflect the old paths from b1 ~> c1 or c2 ~> c3

      // Old subgraph should remain unchanged
      g1.root shouldBe b1.root
      g1.leaves shouldBe c3.leaves // shouldn't reflect the new path from b1 ~> c4 or c2 ~> c5
    }

  }

}
