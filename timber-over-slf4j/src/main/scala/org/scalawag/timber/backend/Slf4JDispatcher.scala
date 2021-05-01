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

package org.scalawag.timber.backend

import org.scalawag.timber.api.{Dispatcher, Entry, Message}
import org.slf4j.{MarkerFactory, LoggerFactory}
import org.scalawag.timber.api.impl._
import org.scalawag.timber.api.style.slf4j.Level._

class Slf4JDispatcher extends Dispatcher {

  override def dispatch(entry: Entry) {
    val logger = LoggerFactory.getLogger(entry.loggingClass.toString)
    val message: Message = entry.message.getOrElse("")
    val level = entry.level.getOrElse(INFO).intValue // TODO: way to specify the default level if none is specified?

    entry.tags.headOption.map(tag => MarkerFactory.getMarker(tag.getClass.getName)) match {

      case Some(marker) =>
        if (level >= ERROR) {
          if (logger.isErrorEnabled)
            logger.error(marker, message.text)
        } else if (level >= WARN) {
          if (logger.isWarnEnabled)
            logger.warn(marker, message.text)
        } else if (level >= INFO) {
          if (logger.isInfoEnabled)
            logger.info(marker, message.text)
        } else if (level >= DEBUG) {
          if (logger.isDebugEnabled)
            logger.debug(marker, message.text)
        } else {
          if (logger.isTraceEnabled)
            logger.trace(marker, message.text)
        }

      case None =>
        if (level >= ERROR) {
          if (logger.isErrorEnabled)
            logger.error(message.text)
        } else if (level >= WARN) {
          if (logger.isWarnEnabled)
            logger.warn(message.text)
        } else if (level >= INFO) {
          if (logger.isInfoEnabled)
            logger.info(message.text)
        } else if (level >= DEBUG) {
          if (logger.isDebugEnabled)
            logger.debug(message.text)
        } else {
          if (logger.isTraceEnabled)
            logger.trace(message.text)
        }

    }

  }
}
