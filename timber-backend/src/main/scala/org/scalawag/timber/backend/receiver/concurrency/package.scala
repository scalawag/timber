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

package org.scalawag.timber.backend.receiver

/** Contains the composable [[Receiver]] concurrency policies available in timber.  These determine whether
  * concurrent access to the various Receiver methods is allowed.
  *
  * Each of the policies has two flavors.  The first is a trait that can be used to mixin to a [[StackableReceiver]].
  * The second is an object that can be used as an argument to the `file()` method in the
  * [[org.scalawag.timber.backend.dispatcher.configuration.dsl DSL]].
  */

package object concurrency
