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

import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition.{AcceptAll, NotCondition}
import org.scalawag.timber.backend.receiver.buffering.ImmediateFlushing
import org.scalawag.timber.backend.receiver.concurrency.Locking
import org.scalawag.timber.backend.receiver.formatter.{DefaultEntryFormatter, EntryFormatter}
import org.scalawag.timber.backend.receiver._
import java.io.FileWriter

package object dsl {

  val level = IntConditionFactory("level") { entry =>
    entry.level.map(_.map(_.intValue))
  }

  val message = StringConditionFactory("message") { entry =>
    entry.message.map(_.map(msg => msg.text))
  }

  val sourceFile = StringConditionFactory("sourceFile") { entry =>
    entry.sourceFile.map(_.toIterable)
  }

  val loggingClass = StringConditionFactory("loggingClass") { entry =>
    entry.loggingClass.map(_.toIterable)
  }

  def logger(attribute: String) =
    StringConditionFactory(s"""logger("$attribute")""") { entry =>
      for {
        attrs <- entry.loggerAttributes
        attrValue <- attrs.get(attribute)
      } yield {
        Iterable(attrValue.toString)
      }
    }

  class ThreadAttribute private[dsl] (attribute: String) {

    def any =
      StringConditionFactory(s"""thread("$attribute").any""") { entry =>
        entry.threadAttributes flatMap { threadAttrs =>
          threadAttrs.get(attribute) flatMap { threadAttrStack =>
            Some(threadAttrStack)
          }
        }
      }

    def top =
      StringConditionFactory(s"""thread("$attribute").top""") { entry =>
        entry.threadAttributes flatMap { threadAttrs =>
          threadAttrs.get(attribute) flatMap { threadAttrStack =>
            Some(threadAttrStack.headOption)
          }
        }
      }
  }

  object thread {
    def apply(attribute: String) = new ThreadAttribute(attribute)

    val name = StringConditionFactory("thread.name") { entry =>
      entry.threadName map { t =>
        Iterable(t)
      }
    }
  }

  def stdout(implicit formatter: EntryFormatter = DefaultEntryFormatter) =
    Locking(new ConsoleOutReceiver(formatter))
  def stderr(implicit formatter: EntryFormatter = DefaultEntryFormatter) =
    Locking(ImmediateFlushing(new ConsoleErrReceiver(formatter)))

  def file(filename: String)(implicit formatter: EntryFormatter = DefaultEntryFormatter) =
    new WriterBasedStackableReceiver(new FileWriter(filename, true))(formatter)

  // These are not on the Subgraph* companion objects because we want the compiler to try them even when it doesn't
  // know that it's looking for a subgraph.  This means that the caller needs to have imported the dsl package, but
  // we're expecting that they'll be doing that anyway.

  implicit def receiverToSubgraph(receiver: Receiver) = Subgraph(receiver)
  implicit def booleanToSubgraph(matches: Boolean) = Subgraph(matches)
  implicit def conditionToSubgraph(condition: Condition) = Subgraph(condition)

  //-------------------------------------------------------------------------------------------------------------------
  // Fanout splits a stream of entries
  //-------------------------------------------------------------------------------------------------------------------

  def fanout(nexts: Subgraph[MutableVertex]*): Subgraph[MutableVertex] = {
    val in = Subgraph(true)
    val outs =
      nexts map { v =>
        in ~> v
      }
    new Subgraph(in.root, outs.flatMap(_.leaves))
  }

  def fanout(nexts: SubgraphWithOutputs[MutableVertexWithOutputs]*): SubgraphWithOutputs[MutableVertexWithOutputs] = {
    val in = Subgraph(true)
    val outs =
      nexts map { v =>
        in ~> v
      }
    new SubgraphWithOutputs(in.root, outs.flatMap(_.leaves))
  }

  //-------------------------------------------------------------------------------------------------------------------
  // Support for choose/when/otherwise
  //-------------------------------------------------------------------------------------------------------------------

  def when(condition: Condition) = new choose.When(condition)

  def otherwise = new choose.When(AcceptAll)

  object choose {

    def apply(whenThens: choose.WhenThen[MutableVertex]*): Subgraph[MutableVertex] = {
      val in = Subgraph(true)

      whenThens.foldLeft(in: SubgraphWithOutputs[MutableVertexWithOutputs]) {
        case (input, whenThen) =>
          // This is not the "otherwise" catch-all.  Split the stream into two based on the filter.
          // Entries that match the filter go to the chain specified
          input ~> whenThen.whenCondition ~> whenThen.thenGraph
          // Other Entries go to the next whenThen (this is the input to the next fold stage)
          input ~> NotCondition(whenThen.whenCondition)
      }

      // resulting chain has all the outputs of all the whenThens
      new Subgraph(in.root, whenThens.flatMap(_.thenGraph.leaves).distinct)
    }

    def apply(
        whenThens: choose.WhenThenWithOutputs[MutableVertexWithOutputs]*
    ): SubgraphWithOutputs[MutableVertexWithOutputs] = {
      val in = Subgraph(true)

      whenThens.foldLeft(in: SubgraphWithOutputs[MutableVertexWithOutputs]) {
        case (input, whenThen) =>
          // This is not the "otherwise" catch-all.  Split the stream into two based on the filter.
          // Entries that match the filter go to the chain specified
          input ~> whenThen.whenCondition ~> whenThen.thenGraph
          // Other Entries go to the next whenThen (this is the input to the next fold stage)
          input ~> NotCondition(whenThen.whenCondition)
      }

      // resulting chain has all the outputs of all the whenThens
      new SubgraphWithOutputs(in.root, whenThens.flatMap(_.thenGraph.leaves).distinct)
    }

    final class WhenThenWithOutputs[+A <: MutableVertexWithOutputs](
        override protected[dsl] val whenCondition: Condition,
        override protected[dsl] val thenGraph: SubgraphWithOutputs[A]
    ) extends WhenThen(whenCondition, thenGraph) {
      def ~>[B <: MutableVertex](next: Subgraph[B]) = new WhenThen(whenCondition, thenGraph ~> next)
      def ~>[B <: MutableVertexWithOutputs](next: SubgraphWithOutputs[B]) =
        new WhenThenWithOutputs(whenCondition, thenGraph ~> next)
      override def toString: String = s"when($whenCondition,${thenGraph.root},${thenGraph.leaves})"
    }

    sealed class WhenThen[+A <: MutableVertex](
        protected[dsl] val whenCondition: Condition,
        protected[dsl] val thenGraph: Subgraph[A]
    ) {
      import thenGraph._
      override def toString: String = s"when($whenCondition,$root,$leaves)"
    }

    final class When(protected[dsl] val whenCondition: Condition) {
      def ~>[A <: MutableVertex](thenGraph: Subgraph[A]) = new WhenThen(whenCondition, thenGraph)
      def ~>[A <: MutableVertexWithOutputs](thenGraph: SubgraphWithOutputs[A]) =
        new WhenThenWithOutputs(whenCondition, thenGraph)
    }
  }
}
