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

import org.scalawag.timber.backend.receiver.Receiver
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

/** The difference between an ImmutableVertex and a MutableVertex is that downstream vertices (outs) can be added to
  * a MutableVertex.  You build up a configuration graph of MutableVertices using the DSL.  Before it can be used
  * to configure an Dispatcher, it has to be made into an ImmutableVertex graph to ensure that it does not
  * change during the Dispatcher's operation.  This is done by copying everything out of the MutableVertices
  * into corresponding ImmutableVertices using [[ImmutableVertex.apply]].
  */

private[configuration] sealed trait ImmutableVertex

private[configuration] object ImmutableVertex {

  /** Turns a graph of MutableVertices (built by the DSL) into a graph of ImmutableVertices (which can be used to
    * configure an Dispatcher).
    */

  def apply(root: MutableVertex): ImmutableVertex = {
    var cache = Map[MutableVertex, ImmutableVertex]()

    def helper(vertex: MutableVertex): ImmutableVertex = {
      cache.get(vertex) match {
        case Some(ie) => ie
        case None =>
          val iv = vertex match {
            case f: MutableConditionVertex =>
              new ImmutableConditionVertex(f.condition, f.nexts.map(helper))
            case e: MutableReceiverVertex =>
              new ImmutableReceiverVertex(e.receiver)
          }
          cache += (vertex -> iv)
          iv
      }
    }

    helper(root)
  }

}

/* Equality works like this.  If two filters have the same condition AND the same outputs, they are considered
 * the same.  That makes sense and it works for us.  If two valves have the same state and the same outputs, they
 * are considered the same.  Since receivers don't actually have any outputs, they are the same if they contain
 * the same Receiver.  That's exactly what we want to happen.  This way, if someone wires up the same filter
 * twice, it will get deduped by the Set.  If someone wires up two exactly equivalent graphs, they'll get deduped as
 * well.
 */

private[configuration] final case class ImmutableConditionVertex(
    val condition: Condition,
    val nexts: Set[ImmutableVertex]
) extends ImmutableVertex {
  override def toString = condition.toString
}

private[configuration] final case class ImmutableReceiverVertex(val receiver: Receiver) extends ImmutableVertex {
  override def toString = receiver.toString
}
