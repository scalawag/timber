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

package org.scalawag.timber.api.style.jul

import org.scalawag.timber.api.{Level => StdLevel}

object Level {
  val FINEST: StdLevel = StdLevel.FINEST
  val FINER: StdLevel = StdLevel.FINER
  val FINE: StdLevel = StdLevel.FINE
  val CONFIG: StdLevel = StdLevel.CONFIG
  val INFO: StdLevel = StdLevel.INFO
  val WARNING: StdLevel = StdLevel.WARNING
  val SEVERE: StdLevel = StdLevel.SEVERE

  val values = Iterable(FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE)
}
