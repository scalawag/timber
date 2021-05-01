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

package org.scalawag.timber.backend.dispatcher.configuration.dsl

import org.scalawag.timber.backend.dispatcher.EntryFacets

object IntConditionFactory {
  def apply(extractionLabel: String)(extractFrom: EntryFacets => Option[Iterable[Int]]) =
    new IntConditionFactory(extractionLabel, extractFrom)
}

class IntConditionFactory(
    override val extractionLabel: String,
    override val extractFrom: EntryFacets => Option[Iterable[Int]]
) extends ConditionFactory[Int](extractionLabel, extractFrom) {

  abstract class IntCondition extends Condition {
    protected def acceptsValue(value: Iterable[Int]): Option[Boolean]
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = extractFrom(entryFacets).flatMap(acceptsValue)
  }

  case class IntGreaterThanOrEqualCondition private[dsl] (val operation: String, val threshold: Int)
      extends IntCondition {
    override protected def acceptsValue(value: Iterable[Int]) = value.map(_ >= threshold).headOption
    override val toString = s"$extractionLabel $operation $threshold"
  }

  case class IntGreaterThanCondition private[dsl] (val operation: String, val threshold: Int) extends IntCondition {
    override protected def acceptsValue(value: Iterable[Int]) = value.map(_ > threshold).headOption
    override val toString = s"$extractionLabel $operation $threshold"
  }

  case class IntLessThanOrEqualCondition private[dsl] (val operation: String, val threshold: Int) extends IntCondition {
    override protected def acceptsValue(value: Iterable[Int]) = value.map(_ <= threshold).headOption
    override val toString = s"$extractionLabel $operation $threshold"
  }

  case class IntLessThanCondition private[dsl] (val operation: String, val threshold: Int) extends IntCondition {
    override protected def acceptsValue(value: Iterable[Int]) = value.map(_ < threshold).headOption
    override val toString = s"$extractionLabel $operation $threshold"
  }

  case class IntEqualsCondition private[dsl] (val operation: String, val target: Int) extends IntCondition {
    override protected def acceptsValue(value: Iterable[Int]) = value.map(_ == target).headOption
    override val toString = s"$extractionLabel $operation $target"
  }

  def <=(thresholdInt: Int) = IntLessThanOrEqualCondition("<=", thresholdInt)
  def <(thresholdInt: Int) = IntLessThanCondition("<", thresholdInt)
  def >=(thresholdInt: Int) = IntGreaterThanOrEqualCondition(">=", thresholdInt)
  def >(thresholdInt: Int) = IntGreaterThanCondition(">", thresholdInt)
  def ===(targetInt: Int) = IntEqualsCondition("===", targetInt)
  def is(targetInt: Int) = IntEqualsCondition("is", targetInt)
}
