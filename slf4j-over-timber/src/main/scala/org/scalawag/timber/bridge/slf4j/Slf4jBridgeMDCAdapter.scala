package org.scalawag.timber.bridge.slf4j

import org.slf4j.spi.MDCAdapter
import org.scalawag.timber.api.LoggingContext
import scala.collection.JavaConversions._

class Slf4jBridgeMDCAdapter extends MDCAdapter {

  override def put(key:String,value:String) {
    LoggingContext.push(key,value)
  }

  override def get(key:String) = {
    LoggingContext.get.get(key).flatMap(_.headOption).getOrElse(null)
  }

  override def remove(key:String) {
    LoggingContext.pop(key,get(key))
  }

  override def clear() {
    LoggingContext.clear
  }

  override def getCopyOfContextMap = {
    LoggingContext.getInnermost
  }

  override def setContextMap(contextMap:java.util.Map[_,_]) {
    LoggingContext.push(contextMap.toMap.asInstanceOf[Map[String,String]])
  }
}
