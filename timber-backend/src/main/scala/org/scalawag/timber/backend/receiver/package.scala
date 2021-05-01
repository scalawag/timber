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

package org.scalawag.timber.backend

/** Contains all of the built-in support for [[org.scalawag.timber.backend.receiver.Receiver receivers]], which are
  * the leaves of dispatcher routing graphs.  Receivers do something with [[org.scalawag.timber.api.Entry entries]].
  * Exactly what they do depends on the implementation.
  */

package object receiver
