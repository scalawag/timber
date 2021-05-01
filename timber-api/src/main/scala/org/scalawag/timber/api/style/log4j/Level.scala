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

package org.scalawag.timber.api.style.log4j

import org.scalawag.timber.api.{Level => StdLevel}

object Level {
  val TRACE: StdLevel = StdLevel.TRACE
  val DEBUG: StdLevel = StdLevel.DEBUG
  val INFO: StdLevel = StdLevel.INFO
  val WARN: StdLevel = StdLevel.WARN
  val ERROR: StdLevel = StdLevel.ERROR
  val FATAL: StdLevel = StdLevel.FATAL

  val values = Iterable(TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
}
