package org.scalawag.timber.bridge.slf4j

import org.scalawag.timber.api.Tag
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import java.io.PrintWriter
import org.scalawag.timber.api.Message

class MarkerTag(val marker:Marker) extends Tag

object Slf4jBridgeLoggerAdapter {
  private def fmt(format:String,args:Array[Object]) = MessageFormatter.arrayFormat(format,args).getMessage
  private def fmt(format:String,args:Object*) = MessageFormatter.arrayFormat(format,args.toArray).getMessage

  implicit private def markerToTag(m:Marker):Tag = new MarkerTag(m)

  implicit private def stringAndThrowableToMessage(msg:(String,Throwable)):Message = Message.messageGatherer { pw:PrintWriter =>
    pw.println(msg._1)
    msg._2.printStackTrace(pw)
  }
}

class Slf4jBridgeLoggerAdapter(private val logger:org.scalawag.timber.api.slf4j.Logger) extends org.slf4j.Logger {
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

  def trace(marker:Marker,msg:String,t:Throwable) = logger.trace((msg,t),marker)
  def trace(marker:Marker,format:String,args:Array[Object]) = logger.trace(fmt(format,args),marker)
  def trace(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.trace(fmt(format,arg1,arg2),marker)
  def trace(marker:Marker,format:String,arg:Object) = logger.trace(fmt(format,arg),marker)
  def trace(marker:Marker,msg:String) = logger.trace(msg,marker)
  def trace(msg:String,t:Throwable) = logger.trace((msg,t))
  def trace(format:String,args:Array[Object]) = logger.trace(fmt(format,args))
  def trace(format:String,arg1:Object,arg2:Object) = logger.trace(fmt(format,arg1,arg2))
  def trace(format:String,arg:Object) = logger.trace(fmt(format,arg))
  def trace(msg:String) = logger.trace(msg)

  def debug(marker:Marker,msg:String,t:Throwable) = logger.debug((msg,t),marker)
  def debug(marker:Marker,format:String,args:Array[Object]) = logger.debug(fmt(format,args),marker)
  def debug(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.debug(fmt(format,arg1,arg2),marker)
  def debug(marker:Marker,format:String,arg:Object) = logger.debug(fmt(format,arg),marker)
  def debug(marker:Marker,msg:String) = logger.debug(msg,marker)
  def debug(msg:String,t:Throwable) = logger.debug((msg,t))
  def debug(format:String,args:Array[Object]) = logger.debug(fmt(format,args))
  def debug(format:String,arg1:Object,arg2:Object) = logger.debug(fmt(format,arg1,arg2))
  def debug(format:String,arg:Object) = logger.debug(fmt(format,arg))
  def debug(msg:String) = logger.debug(msg)

  def info(marker:Marker,msg:String,t:Throwable) = logger.info((msg,t),marker)
  def info(marker:Marker,format:String,args:Array[Object]) = logger.info(fmt(format,args),marker)
  def info(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.info(fmt(format,arg1,arg2),marker)
  def info(marker:Marker,format:String,arg:Object) = logger.info(fmt(format,arg),marker)
  def info(marker:Marker,msg:String) = logger.info(msg,marker)
  def info(msg:String,t:Throwable) = logger.info((msg,t))
  def info(format:String,args:Array[Object]) = logger.info(fmt(format,args))
  def info(format:String,arg1:Object,arg2:Object) = logger.info(fmt(format,arg1,arg2))
  def info(format:String,arg:Object) = logger.info(fmt(format,arg))
  def info(msg:String) = logger.info(msg)

  def warn(marker:Marker,msg:String,t:Throwable) = logger.warn((msg,t),marker)
  def warn(marker:Marker,format:String,args:Array[Object]) = logger.warn(fmt(format,args),marker)
  def warn(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.warn(fmt(format,arg1,arg2),marker)
  def warn(marker:Marker,format:String,arg:Object) = logger.warn(fmt(format,arg),marker)
  def warn(marker:Marker,msg:String) = logger.warn(msg,marker)
  def warn(msg:String,t:Throwable) = logger.warn((msg,t))
  def warn(format:String,args:Array[Object]) = logger.warn(fmt(format,args))
  def warn(format:String,arg1:Object,arg2:Object) = logger.warn(fmt(format,arg1,arg2))
  def warn(format:String,arg:Object) = logger.warn(fmt(format,arg))
  def warn(msg:String) = logger.warn(msg)
  
  def error(marker:Marker,msg:String,t:Throwable) = logger.error((msg,t),marker)
  def error(marker:Marker,format:String,args:Array[Object]) = logger.error(fmt(format,args),marker)
  def error(marker:Marker,format:String,arg1:Object,arg2:Object) = logger.error(fmt(format,arg1,arg2),marker)
  def error(marker:Marker,format:String,arg:Object) = logger.error(fmt(format,arg),marker)
  def error(marker:Marker,msg:String) = logger.error(msg,marker)
  def error(msg:String,t:Throwable) = logger.error((msg,t))
  def error(format:String,args:Array[Object]) = logger.error(fmt(format,args))
  def error(format:String,arg1:Object,arg2:Object) = logger.error(fmt(format,arg1,arg2))
  def error(format:String,arg:Object) = logger.error(fmt(format,arg))
  def error(msg:String) = logger.error(msg)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
