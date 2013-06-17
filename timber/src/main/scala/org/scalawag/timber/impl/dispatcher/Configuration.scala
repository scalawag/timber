package org.scalawag.timber.impl.dispatcher

import org.scalawag.timber.impl._
import org.scalawag.timber.impl.ImmutableFilter
import org.scalawag.timber.impl.ImmutableValve
import org.scalawag.timber.impl.ImmutableReceiver
import receiver.EntryReceiver
import scala.Some
import org.scalawag.timber.dsl.{Vertex, ParameterizedCondition, Condition}
import org.scalawag.timber.api.impl.Entry

case class Configuration(roots:Set[ImmutableVertex]) extends InternalLogging {

  /** Transforms this vertex and all of its descendants (all vertices reachable through its "outs").  It is
    * guaranteed that each vertex will be visited exactly once, <em>after</em> all of its outs have been processed.
    * When an vertex is reachable through multiple paths, it will be transformed once and the resulting vertex will
    * be placed at the corresponding locations in all the paths.  A tree must be processed within one call or else
    * it's possible to get duplicates.  So, don't explicitly call flatMap on the children of a vertex and piece it
    * back together yourself.
    *
    * @param prune a function called on each vertex prior to processing its descendents to determine whether
    *              this path needs to be pursued.  If you know that this vertex will be pruned, this method should
    *              return true to avoid processing its descendents.
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
            case v:ImmutableValve => transform(v,v.outs.flatMap(helper))
            case f:ImmutableFilter => transform(f,f.outs.flatMap(helper))
            case r:ImmutableReceiver => transform(r,Set())
          }
          cache += (vertex -> e)
          e
        }
    }

    Configuration(roots.flatMap(helper))
  }

  private def flatMap(fn: (ImmutableVertex,Set[ImmutableVertex]) => Set[ImmutableVertex]):Configuration = {
    flatMap({ _ => false},fn)
  }


  /** Calls the specified function exactly once for each vertex reachable from the specified root vertex.  Even if
    * there are multiple paths to a given vertex, the function will only be called once for that vertex.  The
    * traversal is depth-first, so all of an vertex's successors are guaranteed to be visited before the vertex
    * itself.
    */

  private def visit(prune:ImmutableVertex => Boolean,visit:ImmutableVertex => Unit) {
    var visited = Set[ImmutableVertex]()

    def helper(vertex:ImmutableVertex) {
      if ( ! visited.contains(vertex) ) {
        if ( ! prune(vertex) ) {
          vertex match {
            case v:ImmutableValve => v.outs.map(helper)
            case f:ImmutableFilter => f.outs.map(helper)
            case r:ImmutableReceiver => // no children
          }
        }
        visit(vertex)
        visited += vertex
      }
    }

    roots.foreach(helper)
  }

  private def visit(fn: ImmutableVertex => Unit) {
    visit({ _ => false},fn)
  }

  /** Transforms this vertex and all of its successors (all vertices reachable through its "outs").  It is
    * guaranteed that each vertex will be visited exactly once, <em>after</em> all of its outs have been processed.
    * When an vertex is reachable through multiple paths, it will be transformed once and the resulting vertex will
    * be placed at the corresponding locations in all the paths.  A tree must be processed within one call or else
    * it's possible to get duplicates.  So, don't explicitly call flatMap on the successors of a vertex and piece it
    * back together yourself.
    *
    * @param transform the function used to transform each vertex.  The first argument is the original vertex (pre-
    *                  transformation).  The second argument is the set of successors that the transformed vertex
    *                  should include in its outs.  Note that this may be different from the outs of the original
    *                  vertex that it passed in.  Those should likely be ignored.  The return value should be the
    *                  newly transformed vertex with the child vertices provided.
    */

  private def map(transform: (ImmutableVertex,Set[ImmutableVertex]) => ImmutableVertex):Configuration = {
    var cache = Map[ImmutableVertex,ImmutableVertex]()

    def helper(vertex:ImmutableVertex):ImmutableVertex = cache.get(vertex) match {
      case Some(e) =>
        e
      case None =>
        val e = vertex match {
          case v:ImmutableValve => transform(v,v.outs.map(helper))
          case f:ImmutableFilter => transform(f,f.outs.map(helper))
          case r:ImmutableReceiver => transform(r,Set())
        }
        cache += (vertex -> e)
        e
    }

    Configuration(roots.map(helper))
  }

  def findReceivers(entry:Entry): Set[EntryReceiver] =
    // constraining against a full Entry should always give us a Configuration with only ImmutableReceivers in it
    constrain(PartialEntry(entry)).roots.asInstanceOf[Set[ImmutableReceiver]].map(_.receiver)

  def constrain(entry:Entry): Configuration = constrain(PartialEntry(entry),true)

  /** Constrain the DAG based on the fields present in the PartialEntry.
    *
    * @param entry a PartialEntry containing the fields with which to constrain the DAG
    * @param decisive true if all filters should be treated as conclusive (no abstinence)
    *
    * @return the constrained Configuration
    */

  def constrain(entry:PartialEntry = PartialEntry(),decisive:Boolean = false):Configuration = {

    def invert(b:Boolean) = !b

    def prune(vertex:ImmutableVertex):Boolean = vertex match {

      // A filter that won't pass this entry can be pruned to save us some time.
      // If it's inconclusive _and_ we're in decisive mode, treat as true (prune).  Otherwise, if it's
      // inconclusive, treat as false so that the structure will be maintained for future constraining.

      case f:ImmutableFilter => f.condition.allows(entry).map(invert).getOrElse {
        if ( decisive )
          log.error("condition " + f.condition + " was not decisive against entry " + entry + " but should have been")
        decisive
      }

      // Receivers are never pruned (since they don't care about the constraints).  That's not to say that they
      // won't be pruned because they are unreachable based on the ImmutableFilters.

      case r:ImmutableReceiver => false

      // If the valve is closed, we can go ahead and prune everything from here down.

      case v:ImmutableValve => v.closed
    }

    def transform(vertex:ImmutableVertex,outs:Set[ImmutableVertex]):Set[ImmutableVertex] = vertex match {
      case v:ImmutableValve =>
        if ( v.open )
          outs // everything passes, go directly to outs
        else
          Set() // nothing passes, dead end
      case f:ImmutableFilter =>
        f.condition.allows(entry) match {
          case Some(true) =>
            outs // everything passes (the filter allowed the entry), go directly to outs
          case Some(false) =>
            Set() // nothing passes (the filter disallowed the entry)
          case None =>
            // The filter is inconclusive
            if ( outs.isEmpty )
              Set() // there are no outs, this filter is no longer needed
            else {
              // We should have already pruned this vertex if we're in decisive mode and the filter is inconclusive.
              // So, this check is really just safeguarding against future changes.
              if ( decisive )
                Set()
              else
                Set(f.copy(outs = outs)) // there are outs, leave the filter in place
            }
        }
      case r:ImmutableReceiver =>
        Set(r)
    }

    flatMap(prune,transform)
  }

  def namedVertices:Map[String,ImmutableVertex] = {
    var namedVertices = Map[String,ImmutableVertex]()

    visit { vertex =>
      if ( vertex.name.isDefined )
        namedVertices += ( vertex.name.get -> vertex )
    }

    namedVertices
  }

  def reconfigure(name:String,value:AnyRef) = {

    map { (vertex,outs) =>
      if ( vertex.name.exists( _ == name ) )
        vertex match {
          case v:ImmutableValve =>
            val b = value.asInstanceOf[java.lang.Boolean]
            new ImmutableValve(b,outs,v.name)
          case f:ImmutableFilter =>
            def clone[P <: AnyRef](param:AnyRef) = f.condition.asInstanceOf[ParameterizedCondition[P]].reconfigure(param.asInstanceOf[P])
            val newCondition = clone(value)
            new ImmutableFilter(newCondition,outs,f.name)
          case x =>
            throw new IllegalStateException("unknown named type: " + x.getClass.getName + " - " + x)
        }
      else
        vertex match {
          case v:ImmutableValve => v.copy(outs = outs)
          case f:ImmutableFilter => f.copy(outs = outs)
          case r:ImmutableReceiver => r
        }
    }

  }

}

object Configuration {
  implicit def apply(v:ImmutableVertex):Configuration = Configuration(Set(v))
  implicit def apply(v:Vertex):Configuration = Configuration(ImmutableVertex(v))
  implicit def apply():Configuration = Configuration(Set.empty[ImmutableVertex])
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
