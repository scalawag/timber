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

import org.scalawag.timber.api
import org.scalawag.timber.api.Dispatcher
import org.scalawag.timber.api.level._

class Logger(val name: String)(implicit dispatcher: Dispatcher)
    extends api.BaseLogger(Map("name" -> name))(dispatcher)
    with Finest
    with Finer
    with Fine
    with Config
    with Info
    with Warning
    with Severe
