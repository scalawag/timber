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

import java.util.regex.Pattern

import scala.util.matching.Regex

object StringOrPattern {
  implicit def fromString(s:String) = StringOrPattern(Left(s))
  implicit def fromRegex(re:Regex) = StringOrPattern(Right(re.pattern))
  implicit def fromPattern(p:Pattern) = StringOrPattern(Right(p))
}

private[dsl] case class StringOrPattern(private val chars:Either[String,Pattern]) {
  def matches(in:String) = chars match {
    case Left(s) => in == s
    case Right(p) => p.matcher(in).matches()
  }

  def isContainedIn(in:String) = chars match {
    case Left(s) => in contains s
    case Right(p) => p.matcher(in).find()
  }

  def starts(in:String) = chars match {
    case Left(s) => in startsWith s
    case Right(p) => {
      val matcher = p.matcher(in)
      matcher.find() && matcher.start() == 0
    }
  }

  def ends(in:String) = chars match {
    case Left(s) => in endsWith s
    case Right(p) => {
      val matcher = p.matcher(in)
      matcher.find() && matcher.end() == in.length
    }
  }

  override val toString = chars match {
    case Left(s) => s""""$s""""
    case Right(p) => s""""$p".r"""
  }

  // Workaround for the fact that java patterns don't have a working equals (for equivalency).
  override def equals(that:Any) = this.canEqual(that) && this.toString == that.toString
}
