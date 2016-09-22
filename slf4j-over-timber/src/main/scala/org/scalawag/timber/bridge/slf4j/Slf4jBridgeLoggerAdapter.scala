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

import org.scalawag.timber.api.Tag
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import java.io.PrintWriter
import org.scalawag.timber.api.Message

private[slf4j] class MarkerTag(val marker:Marker) extends Tag {
  override def toString = marker.getName
}

private[slf4j] object Slf4jBridgeLoggerAdapter {
  private def fmt(format:String,args:Array[Object]) = MessageFormatter.arrayFormat(format,args).getMessage

  private def fmt(format:String,args:Object*) = MessageFormatter.arrayFormat(format,args.toArray).getMessage

  private def fmt(msg:String,throwable:Throwable):Message = Message.stringAndThrowableToMessage(msg,throwable)

  implicit private def markerToTags(m:Marker):TraversableOnce[Tag] = Iterable(new MarkerTag(m))
}

private[slf4j] class Slf4jBridgeLoggerAdapter(private val logger:org.scalawag.timber.api.style.slf4j.Logger) extends org.slf4j.Logger {
  import Slf4jBridgeLoggerAdapter._

  def getName() = logger.name

  def isTraceEnabled() = true
  def isTraceEnabled(marker:Marker) = true
  def isDebugEnabled() = true
  def isDebugEnabled(marker:Marker) = true
  def isInfoEnabled() = true
  def isInfoEnabled(marker:Marker) = true
  def isWarnEnabled() = true
  def isWarnEnabled(marker:Marker) = true
  def isErrorEnabled() = true
  def isErrorEnabled(marker:Marker) = true

  def trace(marker:Marker,msg:String,t:Throwable) = logger.trace(marker)(fmt(msg,t))
  def trace(marker:Marker,format:String,args:Array[Object]) = logger.trace(marker)(fmt(format,args))
  def trace(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.trace(marker)(fmt(format,arg1,arg2))
  def trace(marker:Marker,format:String,arg:Object) = logger.trace(marker)(fmt(format,arg))
  def trace(marker:Marker,msg:String) = logger.trace(marker)(msg)
  def trace(msg:String,t:Throwable) = logger.trace(fmt(msg,t))
  def trace(format:String,args:Array[Object]) = logger.trace(fmt(format,args))
  def trace(format:String,arg1:Object,arg2:Object) = logger.trace(fmt(format,arg1,arg2))
  def trace(format:String,arg:Object) = logger.trace(fmt(format,arg))
  def trace(msg:String) = logger.trace(msg)

  def debug(marker:Marker,msg:String,t:Throwable) = logger.debug(marker)(fmt(msg,t))
  def debug(marker:Marker,format:String,args:Array[Object]) = logger.debug(marker)(fmt(format,args))
  def debug(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.debug(marker)(fmt(format,arg1,arg2))
  def debug(marker:Marker,format:String,arg:Object) = logger.debug(marker)(fmt(format,arg))
  def debug(marker:Marker,msg:String) = logger.debug(marker)(msg)
  def debug(msg:String,t:Throwable) = logger.debug(fmt(msg,t))
  def debug(format:String,args:Array[Object]) = logger.debug(fmt(format,args))
  def debug(format:String,arg1:Object,arg2:Object) = logger.debug(fmt(format,arg1,arg2))
  def debug(format:String,arg:Object) = logger.debug(fmt(format,arg))
  def debug(msg:String) = logger.debug(msg)

  def info(marker:Marker,msg:String,t:Throwable) = logger.info(marker)(fmt(msg,t))
  def info(marker:Marker,format:String,args:Array[Object]) = logger.info(marker)(fmt(format,args))
  def info(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.info(marker)(fmt(format,arg1,arg2))
  def info(marker:Marker,format:String,arg:Object) = logger.info(marker)(fmt(format,arg))
  def info(marker:Marker,msg:String) = logger.info(marker)(msg)
  def info(msg:String,t:Throwable) = logger.info(fmt(msg,t))
  def info(format:String,args:Array[Object]) = logger.info(fmt(format,args))
  def info(format:String,arg1:Object,arg2:Object) = logger.info(fmt(format,arg1,arg2))
  def info(format:String,arg:Object) = logger.info(fmt(format,arg))
  def info(msg:String) = logger.info(msg)

  def warn(marker:Marker,msg:String,t:Throwable) = logger.warn(marker)(fmt(msg,t))
  def warn(marker:Marker,format:String,args:Array[Object]) = logger.warn(marker)(fmt(format,args))
  def warn(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.warn(marker)(fmt(format,arg1,arg2))
  def warn(marker:Marker,format:String,arg:Object) = logger.warn(marker)(fmt(format,arg))
  def warn(marker:Marker,msg:String) = logger.warn(marker)(msg)
  def warn(msg:String,t:Throwable) = logger.warn(fmt(msg,t))
  def warn(format:String,args:Array[Object]) = logger.warn(fmt(format,args))
  def warn(format:String,arg1:Object,arg2:Object) = logger.warn(fmt(format,arg1,arg2))
  def warn(format:String,arg:Object) = logger.warn(fmt(format,arg))
  def warn(msg:String) = logger.warn(msg)

  def error(marker:Marker,msg:String,t:Throwable) = logger.error(marker)(fmt(msg,t))
  def error(marker:Marker,format:String,args:Array[Object]) = logger.error(marker)(fmt(format,args))
  def error(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.error(marker)(fmt(format,arg1,arg2))
  def error(marker:Marker,format:String,arg:Object) = logger.error(marker)(fmt(format,arg))
  def error(marker:Marker,msg:String) = logger.error(marker)(msg)
  def error(msg:String,t:Throwable) = logger.error(fmt(msg,t))
  def error(format:String,args:Array[Object]) = logger.error(fmt(format,args))
  def error(format:String,arg1:Object,arg2:Object) = logger.error(fmt(format,arg1,arg2))
  def error(format:String,arg:Object) = logger.error(fmt(format,arg))
  def error(msg:String) = logger.error(msg)
}

