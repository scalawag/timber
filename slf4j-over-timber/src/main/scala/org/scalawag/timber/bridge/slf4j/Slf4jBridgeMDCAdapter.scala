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

import org.slf4j.spi.MDCAdapter
import org.scalawag.timber.api.ThreadAttributes

private[slf4j] class Slf4jBridgeMDCAdapter extends MDCAdapter {

  override def put(key: String, value: String) {
    ThreadAttributes.push(key, value)
  }

  override def get(key: String): String = {
    ThreadAttributes.get.get(key).flatMap(_.headOption).getOrElse(null)
  }

  override def remove(key: String): Unit = {
    ThreadAttributes.pop(key, get(key))
  }

  override def clear(): Unit = {
    ThreadAttributes.clear
  }

  override def getCopyOfContextMap: java.util.Map[String, String] = {
    // Java bridging...
    ThreadAttributes.getTopmost.foldLeft(new java.util.HashMap[String, String](8)) {
      case (acc, (k, v)) =>
        acc.put(k, v)
        acc
    }
  }

  override def setContextMap(contextMap: java.util.Map[_, _]) {
    // Java bridging...
    var m: Map[String, String] = Map.empty
    contextMap.forEach(new java.util.function.BiConsumer[Any, Any] {
      override def accept(k: Any, v: Any): Unit = k.toString -> v.toString
    })
    ThreadAttributes.push(m)
  }
}
