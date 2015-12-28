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

package org.scalawag.timber.backend.receiver.formatter

import org.scalawag.timber.api.Entry

/** Converts something into text.  Implementations of this trait must be thread-safe.
  *
  * @tparam T the type of object that this formatter can turn into text.
  */
trait Formatter[-T] {
  def format(value:T):String
}

/** Converts an [[Entry]] into text. */

trait EntryFormatter extends Formatter[Entry]

