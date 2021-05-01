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

import org.scalawag.timber.api.Tag
import org.scalawag.timber.backend.dispatcher.EntryFacets

object tagged {
  case class TaggedCondition private[dsl] (val tag: Tag) extends Condition {
    override def accepts(entryFacets: EntryFacets): Option[Boolean] = entryFacets.tags.map(_.contains(tag))
    override val toString = "tagged(%s)".format(tag)
  }

  def apply(t: Tag) = new TaggedCondition(t)
}
