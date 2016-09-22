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

package org.scalawag.timber.backend.dispatcher.configuration

import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.Receiver
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Subgraph
import org.scalawag.timber.backend.dispatcher.EntryFacets

/**
  * Represents a dispatcher configuration graph.  To create a configuration, use the DSL to create a routing graph
  * and then call one of the companion object's factory methods with the desired root vertex (or a Subgraph whose
  * head is the desired root vertex).
  *
  * Example:
  * {{{
  *   import org.scalawag.timber.backend.dispatcher.configuration.Configuration
  *   import org.scalawag.timber.backend.dispatcher.configuration.dsl._
  *   import org.scalawag.timber.api.Level._
  *
  *   Configuration( ( level >= INFO ) ~> stderr )
  * }}}
  *
  * Configuration instances are immutable.  The routing graph created with the DSL may be amended after creating a
  * Configuration based on it but the Configuration creates a snapshot of the graph at the time of its construction.
  */

case class Configuration private[dispatcher] (roots:Set[ImmutableVertex]) {

  /** Transforms this vertex and all of its descendants (all vertices reachable through its "outs").  It is
    * guaranteed that each vertex will be visited exactly once, <em>after</em> all of its outs have been processed.
    * When an vertex is reachable through multiple paths, it will be transformed once and the resulting vertex will
    * be placed at the corresponding locations in all the paths.  A tree must be processed within one call or else
    * it's possible to get duplicates.  So, don't explicitly call flatMap on the children of a vertex and piece it
    * back together yourself.
    *
    * @param prune a function called on each vertex prior to processing its descendants to determine whether
    *              this path needs to be pursued.  If you know that this vertex will be pruned, this method should
    *              return true to avoid processing its descendants.
    * @param transform the function used to transform each vertex.  The first argument is the original vertex (pre-
    *                  transformation).  The second argument is the set of successors that the transformed vertex
    *                  should include in its outs.  Note that this may be different from the successors of the original
    *                  vertex that it passed in.  Those should likely be ignored.  The return value should be the
    *                  newly transformed vertices with which the source vertex should be replaced.
    */

  private def flatMap(prune:ImmutableVertex => Boolean,transform:(ImmutableVertex,Set[ImmutableVertex]) => Set[ImmutableVertex]):Configuration = {
    var cache = Map[ImmutableVertex,Set[ImmutableVertex]]()

    def helper(vertex:ImmutableVertex):Set[ImmutableVertex] = cache.get(vertex) match {
      case Some(e) =>
        e
      case None =>
        if ( prune(vertex) ) {
          Set()
        } else {
          val e = vertex match {
            case f:ImmutableConditionVertex => transform(f,f.nexts.flatMap(helper))
            case r:ImmutableReceiverVertex => transform(r,Set())
          }
          cache += (vertex -> e)
          e
        }
    }

    Configuration(roots.flatMap(helper))
  }

  def findReceivers(entry:Entry): Set[Receiver] =
    // constraining in conclusive mode should always give us a Configuration with only ImmutableReceivers in it
    constrain(entry,true).roots.asInstanceOf[Set[ImmutableReceiverVertex]].map(_.receiver)

  /** Constrain the DAG based on the fields present in the EntryFacets.
    *
    * @param entryFacets an EntryFacets containing the fields with which to constrain the DAG
    * @param decisive true if the entry should be considered complete and therefore filter abstinence should be
    *                 treated as rejection
    *
    * @return the constrained Configuration
    */

  def constrain(entryFacets:EntryFacets = EntryFacets.Empty, decisive:Boolean = false):Configuration = {

    def invert(b:Boolean) = !b

    def prune(vertex:ImmutableVertex):Boolean = vertex match {

      // A filter that doesn't match this entry can be pruned. If it's inconclusive _and_ we're in decisive mode, go
      // ahead and prune it.  Otherwise, if it's inconclusive, we must not prune so that the decision structure can
      // be preserved for future constrain calls.

      case f:ImmutableConditionVertex => f.condition.accepts(entryFacets).map(invert).getOrElse(decisive)

      // A receiver is never pruned since its behavior isn't conditional on the entry being analyzed.  That's
      // not to say it won't be removed later anyway if it's not reachable from any of the root vertices.

      case r:ImmutableReceiverVertex => false

    }

    def transform(vertex:ImmutableVertex,outs:Set[ImmutableVertex]):Set[ImmutableVertex] = vertex match {

      // For a filter to make it past the prune phase, one of the following must be true:
      //  - it conclusively accepts the entry, or
      //  - it abstained and we're not in decisive mode.
      // In the former case, we can treat it like an open valve and skip directly to its outs.
      // In the latter case, we either have to maintain the condition (with its new children) for future constrain
      // calls or, if it has no new children, can drop the filter altogether.

      case f:ImmutableConditionVertex =>
        // We only need to distinguish between the two cases above here, so our logic looks kind of lax here.
        if ( outs.isEmpty )
          Set.empty // If there are no longer any outs, we can just drop the filter altogether.
        else if ( f.condition.accepts(entryFacets).isDefined )
          outs // The condition is decisively true, we can skip directly to its new outs.
        else
          Set(f.copy(nexts = outs)) // Otherwise, make a new condition with the new outs and the old condition.

      // Receivers are always maintained during constrain calls.

      case r:ImmutableReceiverVertex => Set(r)
    }

    flatMap(prune,transform)
  }
/*
  /** Removes redundant or contradictory vertices from the graph.  Constraining a configuration graph only takes care
    * of obvious inefficiencies like removing closed valves, conditions that are always true and paths that don't
    * end in a receiver.  Optimization takes more analysis (and therefore more time) but removes additional
    * inefficiencies that constrain can not.  For example, ( level > 5 and level < 3 ) can be pruned since they can't
    * both be true.  Also, (level > 3 and level > 2) can be simplified to just (level > 2) since that includes levels
    * greater than 3.
   */

  def optimized = {
    // yet to be implemented.
  }
*/
}

object Configuration {
  /** An empty configuration (one that routes doesn't route entries anywhere). */
  val empty = Configuration(Set.empty[ImmutableVertex])
  /** Creates a Configuration based on a routing graph. */
  implicit def apply(g:Subgraph[_]):Configuration = Configuration(Set(ImmutableVertex(g.root)))
}

