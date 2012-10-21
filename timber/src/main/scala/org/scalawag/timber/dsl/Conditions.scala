package org.scalawag.timber.dsl

import java.util.regex.Pattern
import org.scalawag.timber.api.Tag
import util.matching.Regex
import org.scalawag.timber.impl.PartialEntry

sealed trait Condition {
  /** Tests the condition against the PartialEntry specified.  It should always return the same thing given the
    * same input PartialEntry.
    *
    * @return Some(true) or Some(false) if the test is decisive and None if it can't be sure given the PartialEntry.
    *         None will be treated as 'false' for evaluation purposes.
    */

  def allows(candidate:PartialEntry):Option[Boolean]
}

trait ParameterizedCondition[P] extends Condition {
  val parameterType:Class[P]
  val parameterValue:P
  def reconfigure(parameter:P):Condition
}

case class LoggerPrefixCondition(val prefix:String) extends Condition {
  override def allows(entry:PartialEntry):Option[Boolean] = entry.logger.map(_.startsWith(prefix))
  override lazy val toString = """logger.startsWith("%s")""".format(prefix)
}

case class NotCondition(val condition:Condition) extends Condition {
  override def allows(entry:PartialEntry):Option[Boolean] = condition.allows(entry).map(!_)
  override val toString = "not(" + condition + ")"
}

trait LevelCondition extends ParameterizedCondition[Int] {
  override val parameterType:Class[Int] = classOf[Int]
}

case class LowestLevelCondition(val threshold:Int) extends LevelCondition {
  override def allows(entry:PartialEntry):Option[Boolean] = entry.level.map( _ >= threshold )
  override val parameterValue:Int = threshold
  override def reconfigure(parameter:Int): LowestLevelCondition = new LowestLevelCondition(parameter)
  override val toString = "level >= " + threshold
}

case class HighestLevelCondition(val threshold:Int) extends LevelCondition {
  override def allows(entry:PartialEntry):Option[Boolean] = entry.level.map( _ <= threshold )
  override val parameterValue:Int = threshold
  override def reconfigure(parameter:Int):HighestLevelCondition = new HighestLevelCondition(parameter)
  override val toString = "level <= " + threshold
}

case class ContextContainsCondition(val key:String,val value:String) extends Condition {
  override def allows(entry:PartialEntry):Option[Boolean] =
    entry.context.map { context =>
      context.get(key) match {
        case Some(stack) => stack.contains(value)
        case None => false
      }
    }

  override lazy val toString = "ContextContains(%s,%s)".format(key,value)
}

case class InnermostContextEqualsCondition(val key:String,val value:String) extends Condition {
  override def allows(entry:PartialEntry):Option[Boolean] =
    entry.context.map { context =>
      context.get(key) match {
        case Some(stack) => stack.headOption.exists(_ == value)
        case None => false
      }
    }

  override lazy val toString = "InnermostContextContains(%s,%s)".format(key,value)
}

case class MessageContainsCondition(val pattern: Pattern) extends Condition {
  def this(pattern:String) = this(Pattern.compile(pattern))
  def this(regex:Regex) = this(regex.pattern)
  override def allows(entry:PartialEntry):Option[Boolean] = entry.message.map( msg => pattern.matcher(msg.text).find )
  override val toString = "MessageContains(%s)".format(pattern)
}

case class MessageMatchesCondition(val pattern:Pattern) extends Condition {
  def this(pattern:String) = this(Pattern.compile(pattern))
  def this(regex:Regex) = this(regex.pattern)
  override def allows(entry:PartialEntry):Option[Boolean] = entry.message.map( msg => pattern.matcher(msg.text).matches )
  override val toString = "MessageMatches(%s)".format(pattern)
}

case class TaggedCondition(val tag:Tag) extends Condition {
  override def allows(entry:PartialEntry):Option[Boolean] = entry.tags.map(_.contains(tag))
  override val toString = "TagPresent(%s)".format(tag)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
