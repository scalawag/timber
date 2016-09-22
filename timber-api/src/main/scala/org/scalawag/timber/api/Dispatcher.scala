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

import org.scalawag.timber.api.impl.DefaultDispatcherLoader

/** Dispatches [[Entry entries]] to their appropriate destination(s). */
trait Dispatcher {
  /** Dispatch an entry to the appropriate destination(s).
    *
    * @param entry the entry to be dispatched.
    */
  def dispatch(entry:Entry): Unit
}

object Dispatcher {
  /** Defines the default dispatcher that is used when a [[BaseLogger logger]] is created and no alternate dispatcher
    * is specified, either explicitly (through a constructor argument) or implicitly (through another implicit
    * [[Dispatcher Dispatcher]] in scope).
    *
    * This is the sole dispatcher that should be used in libraries. For applications which explicitly configure and
    * use the timber backend, it may sometimes be desirable to use an alternate dispatcher.
    */

  implicit def defaultDispatcher = DefaultDispatcherLoader.dispatcher
}

