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

package org.scalawag.timber.api.style.syslog

import org.scalawag.timber.api.{Level => std}

object Level {
  val DEBUG = std.DEBUG
  val INFO = std.INFO
  val NOTICE = std.NOTICE
  val WARNING = std.WARNING
  val ERROR = std.ERROR
  val CRITICAL = std.CRITICAL
  val ALERT = std.ALERT
  val EMERGENCY = std.EMERGENCY

  val values = Iterable(DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERGENCY)
}
