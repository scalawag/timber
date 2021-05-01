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

package org.scalawag.timber.backend.dispatcher

import org.scalawag.timber.api._
import org.scalawag.timber.backend.dispatcher.Dispatcher.CacheKeyExtractor
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition

/** Represents an incomplete view of an [[Entry]].  The entry may be real or hypothetical.  Real entries are sometimes
  * used to generate views of themselves to focus only on the relevant aspects of the entry (e.g., see
  * [[CacheKeyExtractor]]).  EntryFacets representing hypothetical entries are used to query [[Condition]]s to find
  * out whether they would accept an entry based only on knowledge the fields present.
  *
  * For each field of the [[Entry]] class, this class contains a corresponding field that is optional.  The absence
  * of the field from an EntryFacets instance implies nothing.  The value of the field in the corresponding entry
  * is completely unknown and can't be reasoned about.  The presence of the field in the EntryFacets implies that
  * the value of the field in the [[Entry]] is known.  Note that due to the nesting of the [[Option]]s, it is possible
  * that it is known that the value is absent in the [[Entry]].  This is represented by `Some(None)`.
  *
  * @param level optional view of the level field in an Entry instance
  * @param message optional view of the message field in an Entry instance
  * @param sourceFile optional view of the sourceFile field in an Entry instance
  * @param sourceLineNumber optional view of the sourceLineNumber field in an Entry instance
  * @param loggingClass optional view of the loggingClass field in an Entry instance
  * @param loggingMethod optional view of the loggingMethod field in an Entry instance
  * @param tags optional view of the tags field in an Entry instance
  * @param timestamp optional view of the timestamp field in an Entry instance
  * @param threadName optional view of the threadName field in an Entry instance
  * @param loggerAttributes optional view of the loggerAttributes field in an Entry instance
  * @param threadAttributes optional view of the threadAttributes field in an Entry instance
  */
case class EntryFacets(
    level: Option[Option[Level]] = None,
    message: Option[Option[Message]] = None,
    sourceFile: Option[Option[String]] = None,
    sourceLineNumber: Option[Option[Int]] = None,
    loggingClass: Option[Option[String]] = None,
    loggingMethod: Option[Option[String]] = None,
    tags: Option[Set[Tag]] = None,
    timestamp: Option[Long] = None,
    threadName: Option[String] = None,
    loggerAttributes: Option[Map[String, Any]] = None,
    threadAttributes: Option[Map[String, List[String]]] = None
)

object EntryFacets {

  /** An EntryFacets object that knows nothing about the Entry.  This is used (for example) to determine if a Condition
    * will always accept or reject a value regardless of the entry's content.
    */
  val Empty = EntryFacets()

  /** Creates an EntryFacets containing complete knowledge of the fields of the specified Entry.
    *
    * @param entry the entry on which to base the returned EntryFacets
    * @return an EntryFacets with complete knowledge of the entry
    */
  implicit def apply(entry: Entry): EntryFacets =
    new EntryFacets(
      level = Some(entry.level),
      message = Some(entry.message),
      sourceFile = Some(entry.sourceLocation.map(_.filename)),
      loggingClass = Some(entry.loggingClass),
      loggingMethod = Some(entry.loggingMethod),
      tags = Some(entry.tags),
      timestamp = Some(entry.timestamp),
      threadName = Some(entry.threadName),
      loggerAttributes = Some(entry.loggerAttributes),
      threadAttributes = Some(entry.threadAttributes)
    )
}
