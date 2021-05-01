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

/** Contains composable traits that can be used to add many common level-specific log methods to your custom loggers.
  * They are inspired by various legacy logging technologies.
  *
  * Each trait defines several methods for logging and level `val` that can be overridden to control the level at
  * which those methods log entries. See [[Logger]] for an example.
  *
  * Another example that mixes in a ridiculous set of level methods:
  * {{{
  *   import org.scalawag.timber.api
  *   import org.scalawag.timber.api.Dispatcher
  *   import org.scalawag.timber.api.level._
  *
  *   class Logger(override val attributes:Map[String,Any] = Map.empty, override val tags:Set[Tag] = Set.empty)(implicit dispatcher: Dispatcher)
  *     extends BaseLogger(attributes, tags)(dispatcher) with Emergency with Finest with Warning with Warn
  * }}}
  *
  */

package object level
