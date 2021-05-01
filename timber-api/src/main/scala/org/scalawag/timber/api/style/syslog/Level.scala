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

import org.scalawag.timber.api.{Level => StdLevel}

object Level {
  val DEBUG: StdLevel = StdLevel.DEBUG
  val INFO: StdLevel = StdLevel.INFO
  val NOTICE: StdLevel = StdLevel.NOTICE
  val WARNING: StdLevel = StdLevel.WARNING
  val ERROR: StdLevel = StdLevel.ERROR
  val CRITICAL: StdLevel = StdLevel.CRITICAL
  val ALERT: StdLevel = StdLevel.ALERT
  val EMERGENCY: StdLevel = StdLevel.EMERGENCY

  val values = Iterable(DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERGENCY)
}
