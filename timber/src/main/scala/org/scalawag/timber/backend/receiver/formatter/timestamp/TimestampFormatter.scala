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

package org.scalawag.timber.backend.receiver.formatter.timestamp

import java.text.SimpleDateFormat
import java.util.TimeZone
import org.scalawag.timber.backend.receiver.formatter.Formatter

/** Converts timestamps into their string representation using a [[SimpleDateFormat]].
  *
  * @param format a [[http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html SimpleDateFormat format string]]
  * @param tz the optional time zone to use to format timestamps, None means to use the system default
  */
case class SimpleDateFormatTimestampFormatter(format:String,tz:Option[TimeZone] = None) extends Formatter[Long] {
  private[this] val formatter:ThreadLocal[SimpleDateFormat] = new ThreadLocal[SimpleDateFormat] {
    override def initialValue() = {
      val df = new SimpleDateFormat(format)
      tz.foreach(df.setTimeZone)
      df
    }
  }

  override def format(time: Long) = formatter.get.format(time)
}

/** Converts timestamps into their [[https://en.wikipedia.org/wiki/ISO_8601 ISO 8601]] string representation.
  *
  * @param tz the optional time zone to use to format timestamps, None means to use the system default
  */
class ISO8601TimestampFormatter(tz:Option[TimeZone] = None)
  extends SimpleDateFormatTimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ",tz)

/** Converts timestamps into their [[https://en.wikipedia.org/wiki/ISO_8601 ISO 8601]] string representation for the
  * system default time zone.
  */
object ISO8601TimestampFormatter extends ISO8601TimestampFormatter(None) {
  /** Creates a formatter that converts timestamps into their [[https://en.wikipedia.org/wiki/ISO_8601 ISO 8601]]
    * string representation for the specified time zone.
    *
    * @param tz the time zone to use to format timestamps
    */
  def apply(tz:TimeZone) = new ISO8601TimestampFormatter(Some(tz))
}

/** Converts timestamps into a human-readable and sortable (larger units precede smaller units) format.
  *
  * @param tz the optional time zone to use to format timestamps, None means to use the system default
  */
class HumanReadableTimestampFormatter(tz:Option[TimeZone] = None)
  extends SimpleDateFormatTimestampFormatter("yyyy-MM-dd HH:mm:ss.SSS z",tz)

/** Converts timestamps into a human-readable and sortable (larger units precede smaller units) format for the
  * default time zone.
  */
object HumanReadableTimestampFormatter extends HumanReadableTimestampFormatter(None) {
  /** Creates a formatter that converts timestamps into a human-readable and sortable (larger units precede smaller
    * units) format for the specified time zone.
    *
    * @param tz the time zone to use to format timestamps
    */
  def apply(tz:TimeZone) = new HumanReadableTimestampFormatter(Some(tz))
}

/** Converts timestamps into a string containing the number of milliseconds since the Unix epoch in UTC.
  */
object JavaEpochTimestampFormatter extends Formatter[Long] {
  override def format(time: Long) = time.toString
}
