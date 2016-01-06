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

/** Tells the BaseLogger to evaluate the [[org.scalawag.timber.api.Message message]] immediately.  This may be
  * useful if you're referring to `var`s whose values could change between the time that the message is created and
  * the time that it is ultimately evaluated.  This could be once it asynchronously reaches its final destination.
  *
  * If you want your BaseLogger to always evaluate its messages immediately, you can add this tag to the logger when
  * it is constructed.
  */
object ImmediateMessage extends Tag
