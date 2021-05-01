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

package org.scalawag.timber.backend.receiver.formatter

import org.scalawag.timber.backend.receiver.formatter.level.{NameLevelFormatter}
import org.scalawag.timber.backend.receiver.formatter.timestamp.{HumanReadableTimestampFormatter}
import ProgrammableEntryFormatter._

/** Formats the entries for [[org.scalawag.timber.backend.receiver.Receiver receivers]] that require a formatter
  * and don't specify an alternate.
  */

object DefaultEntryFormatter
    extends ProgrammableEntryFormatter(
      Seq(
        entry.timestamp formattedWith HumanReadableTimestampFormatter,
        entry.level formattedWith NameLevelFormatter,
        entry.loggingClass,
        entry.threadName,
        entry.sourceLocation,
        entry.tags formattedWith Commas,
        entry.threadAttributes map TopsOnly formattedWith CommasAndEquals
      )
    )
