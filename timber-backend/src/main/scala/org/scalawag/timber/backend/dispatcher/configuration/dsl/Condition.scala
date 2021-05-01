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

trait Condition {

  /** Tests the condition against the EntryFacets specified.  It should always return the same thing given the
    * same input EntryFacets.
    *
    * If the condition returns a definitive answer when passed an empty EntryFacets, it means that this is the
    * answer regardless of the input and the condition may be optimized out of the configuration graph as a constant.
    *
    * @return Some(true) or Some(false) if the test is decisive and None if it can't be sure given the EntryFacets.
    *         None will be treated as 'false' for evaluation purposes.
    */

  def accepts(entryFacets: EntryFacets): Option[Boolean]

  def and(that: Condition) = Condition.AndCondition(this, that)
  def or(that: Condition) = Condition.OrCondition(this, that)
  def &&(that: Condition) = Condition.AndCondition(this, that)
  def ||(that: Condition) = Condition.OrCondition(this, that)
  def unary_!() = Condition.NotCondition(this)
}

object Condition {

  def apply(accepts: Boolean) = if (accepts) AcceptAll else RejectAll

  trait ConstantCondition extends Condition

  case object AcceptAll extends ConstantCondition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = Some(true)
    override lazy val toString = "true"
  }

  case object RejectAll extends ConstantCondition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = Some(false)
    override lazy val toString = "false"
  }

  trait LogicalOperationCondition extends Condition

  case class NotCondition(val condition: Condition) extends LogicalOperationCondition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = condition.accepts(entryFacets).map(!_)
    override val toString = "not(" + condition + ")"
  }

  case class AndCondition(val conditions: Condition*) extends LogicalOperationCondition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = {
      val votes = conditions.map(_.accepts(entryFacets))
      if (votes.forall(_.isDefined))
        Some(votes.map(_.get).forall(identity))
      else
        None
    }
    override val toString = conditions.map(_.toString).mkString("(", ") and (", ")")
  }

  case class OrCondition(val conditions: Condition*) extends LogicalOperationCondition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = {
      val votes = conditions.map(_.accepts(entryFacets))
      if (votes.forall(_.isDefined))
        Some(votes.map(_.get).exists(identity))
      else
        None
    }
    override val toString = conditions.map(_.toString).mkString("(", ") or (", ")")
  }

}
