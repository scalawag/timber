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

import org.scalawag.timber.backend.dispatcher.EntryFacets

object StringConditionFactory {
  def apply(extractionLabel:String)(extractFrom:EntryFacets => Option[Iterable[String]]) =
    new StringConditionFactory(extractionLabel,extractFrom)
}

class StringConditionFactory(override val extractionLabel:String, override val extractFrom:EntryFacets => Option[Iterable[String]])
  extends ConditionFactory[String](extractionLabel, extractFrom)
{

  abstract class StringCondition extends Condition {
    protected def matchesValue(value:String):Boolean
    override def accepts(entryFacets:EntryFacets):Option[Boolean] = extractFrom(entryFacets).map(_.exists(matchesValue))
  }

  case class StringMatchesCondition private[dsl] (pattern:StringOrPattern,op:String) extends StringCondition {
    override def matchesValue(value:String) = pattern matches value
    override lazy val toString = s"""$extractionLabel $op $pattern"""
  }

  case class StringContainsCondition private[dsl] (pattern:StringOrPattern) extends StringCondition {
    override def matchesValue(value:String) = pattern isContainedIn value
    override lazy val toString = s"""$extractionLabel contains $pattern"""
  }

  case class StringStartsWithCondition private[dsl] (pattern:StringOrPattern) extends StringCondition {
    override protected def matchesValue(value:String) = pattern starts value
    override lazy val toString = s"""$extractionLabel startsWith $pattern"""
  }

  case class StringEndsWithCondition private[dsl] (pattern:StringOrPattern) extends StringCondition {
    override protected def matchesValue(value:String) = pattern ends value
    override lazy val toString = s"""$extractionLabel endsWith $pattern"""
  }

  def is(pattern:StringOrPattern) = StringMatchesCondition(pattern,"is")
  def ===(pattern:StringOrPattern) = StringMatchesCondition(pattern,"===")
  def matches(pattern:StringOrPattern) = StringMatchesCondition(pattern,"matches")
  def contains(pattern:StringOrPattern) = StringContainsCondition(pattern)
  def startsWith(pattern:StringOrPattern) = StringStartsWithCondition(pattern)
  def endsWith(pattern:StringOrPattern) = StringEndsWithCondition(pattern)
}
