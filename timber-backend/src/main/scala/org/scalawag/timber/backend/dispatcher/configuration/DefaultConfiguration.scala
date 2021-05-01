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

package org.scalawag.timber.backend.dispatcher

import org.scalawag.timber.backend.receiver.ConsoleErrReceiver
import org.scalawag.timber.backend.receiver.buffering.ImmediateFlushing
import org.scalawag.timber.backend.receiver.concurrency.Locking
import org.scalawag.timber.backend.receiver.formatter.DefaultEntryFormatter

package object configuration {
  /** Defines the default dispatcher configuration used when no other configuration is specified.  It's not really
    * intended to be used normally.  It writes to stderr and flushes entries immediately.  It does all this so that
    * you'll notice that you forgot to configure your dispatcher and do it.
    */
  val DefaultConfiguration = Configuration(
    Locking(ImmediateFlushing(new ConsoleErrReceiver(DefaultEntryFormatter)))
  )
}
