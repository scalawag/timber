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

object Entry {

  /** Represents the source code location from which an [[Entry entry]] was created.
    *
    * @param filename the base name of the source file containing the log method call
    * @param lineNumber the line number within the source file containing the log method call
    */

  case class SourceLocation(val filename:String,val lineNumber:Int) {
    override val toString = s"$filename:$lineNumber"
  }
}

/** Represents a log-worthy event in the timber logging system.
  *
  * Depending on the origin of the entry, different fields may be present or absent.  For example, information about
  * the entry's origin (sourceLocation, loggingClass, loggingMethod) will normally be present when the entry is
  * created using the timber API but may be absent for entries bridged from other logging systems' APIs.
  *
  * @param level the optional level at which this entry was logged
  * @param message the optional text content of this entry, which may contain multiple lines
  * @param sourceLocation the optional source code location from which this entry was logged
  * @param loggingClass the optional name of the class from which this entry was logged
  * @param loggingMethod the optional name of the method from which this entry was logged
  * @param tags the set of tags that have been associated with this entry
  * @param timestamp the timestamp at which this entry was created, milliseconds since Java epoch UTC
  * @param threadName the name of the thread which created this entry
  * @param loggerAttributes the attributes associated with the logger that created this entry
  * @param threadAttributes the attributes associated with the thread that created this entry
  */

case class Entry (level: Option[Level] = None,
                  message: Option[Message] = None,
                  sourceLocation: Option[Entry.SourceLocation] = None,
                  loggingClass: Option[String] = None,
                  loggingMethod: Option[String] = None,
                  tags: Set[Tag] = Set.empty,
                  timestamp: Long = System.currentTimeMillis,
                  threadName: String = Thread.currentThread.getName,
                  loggerAttributes: Map[String,Any] = Map.empty,
                  threadAttributes: Map[String, List[String]] = Map.empty)

