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

import org.scalawag.timber.api.Tag
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import org.scalawag.timber.api.Message

private[slf4j] class MarkerTag(val marker: Marker) extends Tag {
  override val toString: String = marker.getName
}

private[slf4j] object Slf4jBridgeLoggerAdapter {
  private def fmt(format: String, args: Array[Object]): String =
    MessageFormatter.arrayFormat(format, args).getMessage

  private def fmt(format: String, args: Object*): String =
    MessageFormatter.arrayFormat(format, args.toArray).getMessage

  private def fmt(msg: String, throwable: Throwable): Message =
    Message.stringAndThrowableToMessage(msg, throwable)

  implicit private def markerToTags(m: Marker): Iterable[Tag] =
    Iterable(new MarkerTag(m))
}

private[slf4j] class Slf4jBridgeLoggerAdapter(private val logger: org.scalawag.timber.api.style.slf4j.Logger)
    extends org.slf4j.Logger {
  import Slf4jBridgeLoggerAdapter._

  override def getName: String = logger.name

  override def isTraceEnabled: Boolean = true
  override def isTraceEnabled(marker: Marker): Boolean = true
  override def isDebugEnabled: Boolean = true
  override def isDebugEnabled(marker: Marker): Boolean = true
  override def isInfoEnabled: Boolean = true
  override def isInfoEnabled(marker: Marker): Boolean = true
  override def isWarnEnabled: Boolean = true
  override def isWarnEnabled(marker: Marker): Boolean = true
  override def isErrorEnabled: Boolean = true
  override def isErrorEnabled(marker: Marker): Boolean = true

  override def trace(marker: Marker, msg: String, t: Throwable): Unit =
    logger.trace(marker)(fmt(msg, t))
  override def trace(marker: Marker, format: String, args: Object*): Unit =
    logger.trace(marker)(fmt(format, args))
  override def trace(marker: Marker, format: String, arg1: Object, arg2: Object): Unit =
    logger.trace(marker)(fmt(format, arg1, arg2))
  override def trace(marker: Marker, format: String, arg: Object): Unit =
    logger.trace(marker)(fmt(format, arg))
  override def trace(marker: Marker, msg: String): Unit =
    logger.trace(marker)(msg)
  override def trace(msg: String, t: Throwable): Unit =
    logger.trace(fmt(msg, t))
  override def trace(format: String, args: Object*): Unit =
    logger.trace(fmt(format, args))
  override def trace(format: String, arg1: Object, arg2: Object): Unit =
    logger.trace(fmt(format, arg1, arg2))
  override def trace(format: String, arg: Object): Unit =
    logger.trace(fmt(format, arg))
  override def trace(msg: String): Unit =
    logger.trace(msg)

  override def debug(marker: Marker, msg: String, t: Throwable): Unit =
    logger.debug(marker)(fmt(msg, t))
  override def debug(marker: Marker, format: String, args: Object*): Unit =
    logger.debug(marker)(fmt(format, args))
  override def debug(marker: Marker, format: String, arg1: Object, arg2: Object): Unit =
    logger.debug(marker)(fmt(format, arg1, arg2))
  override def debug(marker: Marker, format: String, arg: Object): Unit =
    logger.debug(marker)(fmt(format, arg))
  override def debug(marker: Marker, msg: String): Unit =
    logger.debug(marker)(msg)
  override def debug(msg: String, t: Throwable): Unit =
    logger.debug(fmt(msg, t))
  override def debug(format: String, args: Object*): Unit =
    logger.debug(fmt(format, args))
  override def debug(format: String, arg1: Object, arg2: Object): Unit =
    logger.debug(fmt(format, arg1, arg2))
  override def debug(format: String, arg: Object): Unit =
    logger.debug(fmt(format, arg))
  override def debug(msg: String): Unit =
    logger.debug(msg)

  override def info(marker: Marker, msg: String, t: Throwable): Unit =
    logger.info(marker)(fmt(msg, t))
  override def info(marker: Marker, format: String, args: Object*): Unit =
    logger.info(marker)(fmt(format, args))
  override def info(marker: Marker, format: String, arg1: Object, arg2: Object): Unit =
    logger.info(marker)(fmt(format, arg1, arg2))
  override def info(marker: Marker, format: String, arg: Object): Unit =
    logger.info(marker)(fmt(format, arg))
  override def info(marker: Marker, msg: String): Unit =
    logger.info(marker)(msg)
  override def info(msg: String, t: Throwable): Unit =
    logger.info(fmt(msg, t))
  override def info(format: String, args: Object*): Unit =
    logger.info(fmt(format, args))
  override def info(format: String, arg1: Object, arg2: Object): Unit =
    logger.info(fmt(format, arg1, arg2))
  override def info(format: String, arg: Object): Unit =
    logger.info(fmt(format, arg))
  override def info(msg: String): Unit =
    logger.info(msg)

  override def warn(marker: Marker, msg: String, t: Throwable): Unit =
    logger.warn(marker)(fmt(msg, t))
  override def warn(marker: Marker, format: String, args: Object*): Unit =
    logger.warn(marker)(fmt(format, args))
  override def warn(marker: Marker, format: String, arg1: Object, arg2: Object): Unit =
    logger.warn(marker)(fmt(format, arg1, arg2))
  override def warn(marker: Marker, format: String, arg: Object): Unit =
    logger.warn(marker)(fmt(format, arg))
  override def warn(marker: Marker, msg: String): Unit =
    logger.warn(marker)(msg)
  override def warn(msg: String, t: Throwable): Unit =
    logger.warn(fmt(msg, t))
  override def warn(format: String, args: Object*): Unit =
    logger.warn(fmt(format, args))
  override def warn(format: String, arg1: Object, arg2: Object): Unit =
    logger.warn(fmt(format, arg1, arg2))
  override def warn(format: String, arg: Object): Unit =
    logger.warn(fmt(format, arg))
  override def warn(msg: String): Unit =
    logger.warn(msg)

  override def error(marker: Marker, msg: String, t: Throwable): Unit =
    logger.error(marker)(fmt(msg, t))
  override def error(marker: Marker, format: String, args: Object*): Unit =
    logger.error(marker)(fmt(format, args))
  override def error(marker: Marker, format: String, arg1: Object, arg2: Object): Unit =
    logger.error(marker)(fmt(format, arg1, arg2))
  override def error(marker: Marker, format: String, arg: Object): Unit =
    logger.error(marker)(fmt(format, arg))
  override def error(marker: Marker, msg: String): Unit =
    logger.error(marker)(msg)
  override def error(msg: String, t: Throwable): Unit =
    logger.error(fmt(msg, t))
  override def error(format: String, args: Object*): Unit =
    logger.error(fmt(format, args))
  override def error(format: String, arg1: Object, arg2: Object): Unit =
    logger.error(fmt(format, arg1, arg2))
  override def error(format: String, arg: Object): Unit =
    logger.error(fmt(format, arg))
  override def error(msg: String): Unit =
    logger.error(msg)
}
