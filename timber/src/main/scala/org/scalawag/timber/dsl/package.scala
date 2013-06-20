package org.scalawag.timber

import org.scalawag.timber.api.{Level, Tag}
import impl.formatter.EntryFormatter
import impl.receiver._
import java.io.File

/* These classes are owned by the user.  timber doesn't ever modify them.  It uses them to build up an equivalent,
 * immutable configuration.  It's equivalent because it may be optimized.  It's immutable so that the user
 * can do things with the DSL classes here and not change the behavior of the logger (until it's reapplied).
 */

package dsl {

  trait Vertex {

    def ::(in:HasOutputs):HasOutputs = {
      checkForCycle(in)
      in.outputs += this
      in
    }

    private def checkForCycle(in:HasOutputs) {
      def findDownstream(in:HasOutputs,out:Vertex):Boolean =
        ( in eq out ) || ( out match {
          case ho:HasOutputs => ho.outputs.exists(findDownstream(in,_))
          case _ => false
        } )

      if ( findDownstream(in,this) )
        throw new IllegalArgumentException("cycle detected in logging configuration")
    }

  }

  trait HasOutputs extends Vertex {
    private[timber] var outputs = Set[Vertex]()
  }

  class Valve(val open:Boolean) extends Vertex with HasOutputs {
    override lazy val toString = open.toString
  }

  class NameableValve(override val open:Boolean) extends Valve(open) {
    def as(name:String) = new NamedValve(open,name)
    def closed = new NameableValve(false)
  }

  class NamedValve(override val open:Boolean,val name:String) extends Valve(open) {
    override lazy val toString = """%s "%s"""".format(open,name)
    def closed = new NamedValve(false,name)
  }

  class Filter(val condition:Condition) extends HasOutputs {
    override lazy val toString = condition.toString
    def unary_!() = new Filter(new NotCondition(condition))
  }

  class NameableFilter(override val condition:ParameterizedCondition[_]) extends Filter(condition) {
    def as(name:String) = new NamedFilter(name,condition)
  }

  class NamedFilter(val name:String,override val condition:Condition) extends Filter(condition) {
    override lazy val toString = """%s "%s"""".format(condition,name)
  }

  class Receiver(val receiver:EntryReceiver) extends Vertex {
    override lazy val toString = receiver.toString
  }
}

package object dsl {

  object logger {
    def startsWith(prefix:String) = new Filter(new LoggerPrefixCondition(prefix))
  }

  object level {
    def <=(level:Int) = new NameableFilter(new HighestLevelCondition(level))
    def >=(level:Int) = new NameableFilter(new LowestLevelCondition(level))
    def <=(level:Level) = new NameableFilter(new HighestLevelCondition(level.level))
    def >=(level:Level) = new NameableFilter(new LowestLevelCondition(level.level))
  }

  object context {
    class ContextKey(val key:String) {
      def is(value:String) = new Filter(new InnermostContextEqualsCondition(key,value))
      def contains(value:String) = new Filter(new ContextContainsCondition(key,value))
    }
    def apply(key:String) = new ContextKey(key)
  }

  def tagged(t:Tag) = new Filter(new TaggedCondition(t))

  def stdout(implicit formatter:EntryFormatter) = Asynchronous(new StdoutReceiver(formatter))
  def stderr(implicit formatter:EntryFormatter) = Asynchronous(new StderrReceiver(formatter))

  def file(filename:String,append:Boolean = true,charset:Option[String] = None)(implicit formatter:EntryFormatter) =
    Asynchronous(new FileAppender(new File(filename),formatter,append,charset))

  def valve = new NameableValve(true)

  implicit def conditionWrapper(condition:Condition):Filter = new Filter(condition)
  implicit def parameterizedConditionWrapper(condition:ParameterizedCondition[_]):NameableFilter = new NameableFilter(condition)
  implicit def entryReceiverWrapper(receiver:EntryReceiver):Receiver = new Receiver(receiver)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
