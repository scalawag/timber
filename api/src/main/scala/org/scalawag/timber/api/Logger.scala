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

import org.scalawag.timber.api.level._

/** Provides a [[org.scalawag.timber.api.BaseLogger BaseLogger]] that can be used out-of-the-box with a default set
  * of level-specific log methods.
  *
  * @param attributes the logger attributes that this logger associates with the entries it creates
  * @param tags the tags that this logger associates with the entries it creates
  */
class Logger(override val attributes:Map[String,Any] = Map.empty, override val tags:Set[Tag] = Set.empty)(implicit dispatcher: Dispatcher)
  extends BaseLogger(attributes, tags)(dispatcher) with Trace with Debug with Info with Warn with Error
