package org.scalawag.timber.dsl.debug

import java.io.PrintWriter
import org.scalawag.timber.impl.{ImmutableValve, ImmutableReceiver, ImmutableFilter, ImmutableVertex}
import org.scalawag.timber.impl.dispatcher.Configuration

private class DotDumper(out:IndentingPrintWriter) {
  private var dumped = Set[String]()

  private def getId(vertex:ImmutableVertex) = vertex.hashCode.toString

  private def dump(configuration:Configuration) {
    out.println("digraph Configuration {")
    out.indent {
      out.println("""ranksep="1.5in";""")
      out.println("{")
      out.indent {
        out.println("""rank="source";""")
        out.println(""""IN" [label="IN",shape="invhouse"];""")
      }
      out.println("}")

      configuration.roots.foreach(dumpElement(_,Some("IN")))
    }
    out.println("}")
  }

  private def dumpEdge(fromId:String,to:ImmutableVertex) {
    val toId = getId(to)
    out.println(""""%s":s -> "%s":n""".format(fromId,toId))
  }

  private def escape(s:String) = s.replaceAllLiterally("\"","\\\"").replaceAll("\n","\\\\n")

  private def dumpVertex(vertex:ImmutableVertex,props:Map[String,String],rank:Option[String] = None) {
    val id = getId(vertex)

    def format(props:Map[String,String]):String = props map { case (k,v) =>
      """%s="%s"""".format(k,escape(v))
    } mkString(",")

    if ( rank.isDefined ) {
      out.println("{")
      out.changeIndent(1)
      out.println("""rank="%s";""".format(rank.get))
    }

    out.println(""""%s" [%s];""".format(id,format(props)))

    if ( rank.isDefined ) {
      out.changeIndent(-1)
      out.println("}")
    }
  }

  private def dumpElement(vertex:ImmutableVertex,parent:Option[String] = None,rankOverride:Option[String] = None) {
    val id = getId(vertex)
    if ( ! dumped.contains(id) ) {

      val (rank,props) = vertex match {
        case f:ImmutableFilter =>
          val label = f.condition.toString + f.name.map("\n\"" + _ + "\"").getOrElse("")
          (None,Map("shape" -> "rect","label" -> label))
        case v:ImmutableValve =>
          val label = v.open.toString + v.name.map("\n\"" + _ + "\"").getOrElse("")
          (None,Map("shape" -> "rect","label" -> label))
        case r:ImmutableReceiver =>
          (Some("sink"),Map("shape" -> "house","label" -> r.receiver.toString))
      }

      dumpVertex(vertex,props,List(rankOverride,rank).flatten.headOption)

      vertex match {
        case o:ImmutableFilter => o.outs.foreach(dumpElement(_,Some(id)))
        case o:ImmutableValve => o.outs.foreach(dumpElement(_,Some(id)))
        case o:ImmutableReceiver => // noop
      }

      dumped += id
    }
    parent.foreach(dumpEdge(_,vertex))
  }
}

object DotDumper {
  def dump(configuration:Configuration,out:PrintWriter) = (new DotDumper(new IndentingPrintWriter(out))).dump(configuration)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
