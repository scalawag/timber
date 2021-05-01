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

package org.scalawag.timber.backend.dispatcher.configuration

import org.scalawag.timber.backend.dispatcher.EntryFacets
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition
import org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition.{AcceptAll, RejectAll}

trait DogmaticConditions {
  protected val TRUE = AcceptAll
  protected val FALSE = RejectAll

  protected val ABSTAIN = new Condition {
    override def accepts(entryFacets: EntryFacets) = None
    override val toString = "abstain"
  }
}
