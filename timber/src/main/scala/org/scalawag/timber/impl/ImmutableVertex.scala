package org.scalawag.timber.impl

import org.scalawag.timber.dsl._
import receiver.EntryReceiver
import scala.Some

object ImmutableVertex {

  def apply(root:Vertex):ImmutableVertex = {
    var cache = Map[Vertex,ImmutableVertex]()

    def helper(vertex:Vertex):ImmutableVertex = {
      cache.get(vertex) match {
        case Some(ie) => ie
        case None =>
          val iv = vertex match {
            case f: NamedFilter =>
              new ImmutableFilter(f.condition,f.outputs.map(helper),Some(f.name))
            case f: Filter =>
              new ImmutableFilter(f.condition,f.outputs.map(helper))
            case v: NamedValve =>
              new ImmutableValve(v.open,v.outputs.map(helper),Some(v.name))
            case v: Valve =>
              new ImmutableValve(v.open,v.outputs.map(helper))
            case e: Receiver =>
              new ImmutableReceiver(e.receiver)
          }
          cache += (vertex -> iv)
          iv
      }
    }

    helper(root)
  }

}

sealed trait ImmutableVertex {
  val name:Option[String]

  /** Removes paths in the graph that can not reach an EntryReceiver.  This currently includes:
    *  <ul>
    *    <li> anything only reachable through a closed ImmutableValve
    *    <li> anything that has no outbound path leading to a EntryReceiver (e.g., one was never attached)
    *  </ul>
    *
    */

  def optimized = {

    def prune(vertex:ImmutableVertex):Boolean = vertex match {
      case v:ImmutableValve => v.closed
      case _ => false
    }

    def transform(vertex:ImmutableVertex,outs:Set[ImmutableVertex]):Set[ImmutableVertex] = vertex match {
      case v:ImmutableValve =>
        if ( outs.isEmpty )
          Set() // nothing passes, dead end
        else
          outs // everything passes, go directly to outs
      case f:ImmutableFilter =>
        if ( outs.isEmpty )
          Set() // nothing passes, dead end
        else {
          Set(new ImmutableFilter(f.condition,outs)) // everything passes, here's where we'd check constraints
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

  def flatMap(prune:ImmutableVertex => Boolean,transform:(ImmutableVertex,Set[ImmutableVertex]) => Set[ImmutableVertex]):ImmutableVertex = {
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

    val tops = helper(this)

    if ( tops.size == 1 )
      tops.head
    else
      new ImmutableValve(true,tops)
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

  def map(transform: (ImmutableVertex,Set[ImmutableVertex]) => ImmutableVertex):ImmutableVertex = {
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

    helper(this)
  }

  /** Calls the specified function exactly once for each vertex reachable from the specified root vertex.  Even if
    * there are multiple paths to a given vertex, the function will only be called once for that vertex.  The
    * traversal is depth-first, so all of an vertex's successors are guaranteed to be visited before the vertex
    * itself.
    */

  def visit(fn: ImmutableVertex => Unit) {
    visit({ _ => false},fn)
  }

  def visit(prune:ImmutableVertex => Boolean,visit:ImmutableVertex => Unit) {
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

    helper(this)
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

/* Equality works like this.  If two filters have the same condition AND the same outputs, they are considered
 * the same.  That makes sense and it works for us.  If two valves have the same state and the same outputs, they
 * are considered the same.  Since receivers don't actually have any outputs, they are the same if they contain
 * the same EntryReceiver.  That's exactly what we want to happen.  This way, if someone wires up the same filter
 * twice, it will get deduped by the Set.  If someone wires up two exactly equivalent graphs, they'll get deduped as
 * well.
 */

case class ImmutableFilter(val condition: Condition, val outs: Set[ImmutableVertex], val name:Option[String] = None) extends ImmutableVertex {
  def allows(entry:PartialEntry):Option[Boolean] = condition.allows(entry)
}

case class ImmutableValve(val open:Boolean, val outs: Set[ImmutableVertex], val name:Option[String] = None) extends ImmutableVertex {
  val closed = !open
}

case class ImmutableReceiver(val receiver:EntryReceiver, val name:Option[String] = None) extends ImmutableVertex

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
