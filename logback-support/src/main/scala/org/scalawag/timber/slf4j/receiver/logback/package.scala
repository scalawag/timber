package org.scalawag.timber.slf4j.receiver

import org.scalawag.timber.impl.formatter.EntryFormatter
import ch.qos.logback.core.{FileAppender, Context}
import ch.qos.logback.core.rolling._
import org.scalawag.timber.api.impl.Entry

package object logback {

  def file(filename:String,
           name:Option[String] = None,
           append:Option[Boolean] = None,
           prudent:Option[Boolean] = None)(implicit formatter:EntryFormatter,context:LogbackContext) = {
    val encoder = new EncoderAdapter(formatter)
    encoder.setContext(context)
    context.add(encoder)

    val appender = new FileAppender[Entry]
    appender.setContext(context)
    appender.setFile(filename)
    appender.setEncoder(encoder)
    name.foreach(appender.setName)
    prudent.foreach(appender.setPrudent)
    append.foreach(appender.setAppend)
    context.add(appender)
    appender
  }

  def rollingFile(filename:String,
                  rollingPolicy:RollingPolicy,
                  triggeringPolicy:Option[TriggeringPolicy[Entry]] = None,
                  name:Option[String] = None,
                  append:Option[Boolean] = None,
                  prudent:Option[Boolean] = None)(implicit formatter:EntryFormatter,context:LogbackContext) = {
    val encoder = new EncoderAdapter(formatter)
    encoder.setContext(context)
    context.add(encoder)

    val appender = new RollingFileAppender[Entry]
    appender.setContext(context)
    appender.setFile(filename)
    appender.setEncoder(encoder)
    rollingPolicy.setParent(appender)
    appender.setRollingPolicy(rollingPolicy)
    triggeringPolicy.foreach(appender.setTriggeringPolicy)
    name.foreach(appender.setName)
    prudent.foreach(appender.setPrudent)
    append.foreach(appender.setAppend)
    context.add(appender)
    appender
  }

  def timeBasedRollingPolicy(fileNamePattern:String,
                             maxHistory:Option[Int] = None,
                             cleanHistoryOnStart:Option[Boolean] = None)(implicit context:LogbackContext) = {
    val policy = new TimeBasedRollingPolicy[Entry]
    policy.setContext(context)
    policy.setFileNamePattern(fileNamePattern)
    maxHistory.foreach(policy.setMaxHistory)
    cleanHistoryOnStart.foreach(policy.setCleanHistoryOnStart)
    context.add(policy)
    policy
  }

  def fixedWindowRollingPolicy(fileNamePattern:String,
                               minIndex:Option[Int] = None,
                               maxIndex:Option[Int] = None)(implicit context:LogbackContext) = {
    val policy = new FixedWindowRollingPolicy
    policy.setContext(context)
    policy.setFileNamePattern(fileNamePattern)
    minIndex.foreach(policy.setMinIndex)
    maxIndex.foreach(policy.setMaxIndex)
    context.add(policy)
    policy
  }

  def sizeBasedTriggeringPolicy(maxFileSize:Option[String] = None)(implicit context:LogbackContext) = {
    val policy = new SizeBasedTriggeringPolicy[Entry]
    policy.setContext(context)
    maxFileSize.foreach(policy.setMaxFileSize)
    context.add(policy)
    policy
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
