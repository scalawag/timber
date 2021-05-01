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

package org.scalawag.timber.bridge.slf4j

import org.slf4j.spi.MDCAdapter
import org.scalawag.timber.api.ThreadAttributes
import scala.collection.JavaConverters._

private[slf4j] class Slf4jBridgeMDCAdapter extends MDCAdapter {

  override def put(key: String, value: String): Unit =
    ThreadAttributes.push(key, value)

  override def get(key: String): String =
    ThreadAttributes.get.get(key).flatMap(_.headOption).orNull

  override def remove(key: String): Unit =
    ThreadAttributes.pop(key, get(key))

  override def clear(): Unit =
    ThreadAttributes.clear

  override def getCopyOfContextMap: java.util.Map[String, String] =
    ThreadAttributes.getTopmost.asJava

  override def setContextMap(contextMap: java.util.Map[_, _]): Unit =
    ThreadAttributes.push(contextMap.asScala.map { case (k, v) => k.toString -> v.toString }.toMap)
}
