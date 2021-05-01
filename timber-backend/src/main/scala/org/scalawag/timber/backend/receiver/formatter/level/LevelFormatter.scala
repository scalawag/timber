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

package org.scalawag.timber.backend.receiver.formatter.level

import org.scalawag.timber.api.Level
import org.scalawag.timber.backend.receiver.formatter.Formatter

/** Uses the name stored with the level (if available) or else uses the integer value. */

object NameLevelFormatter extends Formatter[Level] {
  override def format(level: Level) = level.name.getOrElse(level.intValue.toString)
}

/** Uses the integer value the level corresponds to in all cases. */

object NumberLevelFormatter extends Formatter[Level] {
  override def format(level: Level) = level.intValue.toString
}

/** Translates integer levels into the specified strings regardless of the names embedded in the Levels.
  * If a level is lower than the lowest specified thresholdLevel, the lowest name specified will be used.
  *
  * @param thresholds the thresholds at which to switch to a new level name
  */

class TranslatingLevelFormatter(thresholds: Iterable[Level]) extends Formatter[Level] {
  // Produce a sorted list that will help us efficiently select the level name
  private[this] val thresholdList = {
    // Sort by level number
    val sorted = thresholds.toList.sorted
    // Replace the first entry with Int.MinValue to ensure it catches everything (even below the threshold)
    val firstToMinValue = sorted.head.copy(intValue = Int.MinValue) :: sorted.tail
    // Reverse it, so that we look at the largest threshold first
    firstToMinValue.reverse
  }

  override def format(level: Level) = thresholdList.find(_ <= level).map(_.toString).get
}
