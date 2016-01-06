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

package org.scalawag.timber.api

/** Defines some common log levels that encompass most of the log levels used by legacy logging systems.
  * This object also provides implicit conversions for levels to and from integers.
  */
object Level {
  val FINEST    = Level(10,"FINEST")

  val TRACE     = Level(20,"TRACE")
  val FINER     = Level(20,"FINER")

  val DEBUG     = Level(30,"DEBUG")
  val FINE      = Level(30,"FINE")

  val CONFIG    = Level(40,"CONFIG")

  val INFO      = Level(50,"INFO")

  val NOTICE    = Level(60,"NOTICE")

  val WARN      = Level(70,"WARN")
  val WARNING   = Level(70,"WARNING")

  val ERROR     = Level(80,"ERROR")
  val SEVERE    = Level(80,"SEVERE")

  val FATAL     = Level(90,"FATAL")
  val CRITICAL  = Level(90,"CRITICAL")

  val ALERT     = Level(100,"ALERT")

  val EMERGENCY = Level(110,"EMERGENCY")

  def apply(severity:Int,name:String):Level = apply(severity,Some(name))

  /** Creates an anonymous [[Level]] representing a numeric log intValue. */
  implicit def intToLevel(level:Int) = apply(level)
  /** Allows a [[Level]] to be specified anywhere an Int is expected. The intValue's integer value is used. */
  implicit def levelToInt(level:Level) = level.intValue
}

/** Represents a log level of an [[Entry]] (sometimes known in other systems as "severity").  Levels are always
  * treated as their integer values internally.  The only components that care about the names are the eventual entry
  * destinations, which could be log files or sockets, etc.
  *
  * The names provided by the API are simply hints which the destination <em>may</em> choose use to format the entry.
  * The destinations may also choose to ignore the names and only use the numeric value or even translate the integers
  * to its own labels.
  *
  * @param intValue the numeric value of the level, used for comparison with other levels internally
  * @param name a suggested name to use when ultimately writing entries logged at this level
  */
case class Level(intValue:Int, name:Option[String] = None) extends Ordered[Level] {
  override def compare(that: Level): Int = this.intValue.compareTo(that.intValue)
  override lazy val toString = name.getOrElse(intValue.toString)
}

