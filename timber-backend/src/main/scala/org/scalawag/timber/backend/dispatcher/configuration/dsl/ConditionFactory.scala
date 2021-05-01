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

package org.scalawag.timber.backend.dispatcher.configuration.dsl

import org.scalawag.timber.backend.dispatcher.EntryFacets

class ConditionFactory[A](val extractionLabel: String, val extractFrom: EntryFacets => Option[Iterable[A]]) {

  case object isAbsent extends Condition {
    override def accepts(entryFacets: EntryFacets) = extractFrom(entryFacets).map(_.isEmpty)
    override val toString = s"$extractionLabel isAbsent"
  }

  case object isPresent extends Condition {
    override def accepts(entryFacets: EntryFacets) = extractFrom(entryFacets).map(!_.isEmpty)
    override val toString = s"$extractionLabel isPresent"
  }

}
