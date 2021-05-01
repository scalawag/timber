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

package org.scalawag.timber.backend.receiver

/** Contains the composable [[Receiver]] buffering policies available in timber.  These only affect how often the
  * receiver is flushed by timber.  Many receivers will be based on instances of other classes that are themselves
  * buffered (e.g., BufferedWriters and BufferedOutputStreams).  While the flushes that timber instigates should make
  * it down to the underlying objects, those objects may also flush for other reasons (e.g., their buffer reaching
  * capacity). So, this buffering policy may not be the only source of flushes.
  *
  * Each of the policies has two flavors.  The first is a trait that can be used to mixin to a [[StackableReceiver]].
  * The second is an object that can be used as an argument to the `file()` method in the
  * [[org.scalawag.timber.backend.dispatcher.configuration.dsl DSL]].
  */

package object buffering
