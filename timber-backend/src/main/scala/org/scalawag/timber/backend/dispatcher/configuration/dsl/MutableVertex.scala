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

import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition.ConstantCondition
import org.scalawag.timber.backend.receiver.Receiver

/* These classes are owned by the user.  timber doesn't ever modify them.  It uses them to build up an equivalent,
 * immutable configuration.  It's equivalent because it may be optimized.  It's immutable so that the user
 * can do things with the DSL classes here and not change the behavior of the logger (until it's reapplied).
 */

sealed trait MutableVertex

sealed trait MutableVertexWithOutputs extends MutableVertex {
  private var _outputs = Set[MutableVertex]()
  private[timber] def nexts = _outputs

  private[timber] def addNext(next: MutableVertex) = {
    // Look for a cycle
    def findPath(from: MutableVertex, to: MutableVertex): Boolean =
      (from eq to) || (from match {
        case ho: MutableVertexWithOutputs => ho.nexts.exists(findPath(_, to))
        case _                            => false
      })

    if (findPath(next, this))
      throw new IllegalArgumentException("cycle detected during logging configuration")

    _outputs += next
  }
}

final class MutableConditionVertex(val condition: Condition) extends MutableVertexWithOutputs {
  override lazy val toString = condition.toString
}

// This is not actually mutable but it can take part in a mutable graph.

final class MutableReceiverVertex(val receiver: Receiver) extends MutableVertex {
  override lazy val toString = receiver.toString
}

object Subgraph {
  def apply(accepts: Boolean): SubgraphWithOutputs[MutableConditionVertex] = apply(Condition(accepts))

  def apply(condition: Condition): SubgraphWithOutputs[MutableConditionVertex] = {
    val filter = new MutableConditionVertex(condition)
    new SubgraphWithOutputs(filter, Seq(filter))
  }

  def apply(receiver: Receiver): Subgraph[MutableReceiverVertex] = {
    val r = new MutableReceiverVertex(receiver)
    new Subgraph(r, Seq(r))
  }
}

sealed class Subgraph[+A <: MutableVertex](
    private[backend] val root: MutableVertex,
    private[backend] val leaves: Seq[A]
)

final class SubgraphWithOutputs[+A <: MutableVertexWithOutputs](
    override protected[backend] val root: MutableVertex,
    override protected[backend] val leaves: Seq[A]
) extends Subgraph(root, leaves) {
  def ~>[B <: MutableVertex](next: Subgraph[B]): Subgraph[B] = {
    this.leaves.foreach(_.addNext(next.root))
    new Subgraph(root, next.leaves)
  }

  def ~>[B <: MutableVertexWithOutputs](next: SubgraphWithOutputs[B]): SubgraphWithOutputs[B] = {
    this.leaves.foreach(_.addNext(next.root))
    new SubgraphWithOutputs(root, next.leaves)
  }
}
