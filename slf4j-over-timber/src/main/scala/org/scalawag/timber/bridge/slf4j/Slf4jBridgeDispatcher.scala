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

package org.scalawag.timber.bridge.slf4j

import java.util.concurrent.atomic.AtomicReference

import org.scalawag.timber.api
import org.scalawag.timber.api.Entry
import org.scalawag.timber.api.impl.DefaultDispatcherLoader

object Slf4jBridgeDispatcher extends api.Dispatcher {
  private[this] val dispatcherRef = new AtomicReference[api.Dispatcher](DefaultDispatcherLoader.dispatcher)

  def set(delegate:api.Dispatcher) = dispatcherRef.set(delegate)

  override def dispatch(entry:Entry) = dispatcherRef.get.dispatch(entry)
}
