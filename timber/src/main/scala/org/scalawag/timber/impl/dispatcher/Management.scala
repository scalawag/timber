package org.scalawag.timber.impl.dispatcher

import javax.management._
import org.scalawag.timber.dsl.ParameterizedCondition
import scala.Some
import org.scalawag.timber.impl.{ImmutableReceiver, ImmutableValve, ImmutableFilter, InternalLogging}

trait Management extends Configurable with DynamicMBean with InternalLogging {

  abstract override def onConfigurationChange() {
    super.onConfigurationChange()
    mbeanInfo = None
  }

  private def parameterValueToJmxValue(v:Any):AnyRef = v match {
    case v:Object => v
    case n:Int => n:java.lang.Integer
    case b:Boolean => b:java.lang.Boolean
  }

  private def jmxValueToParameterValue[T:Manifest](value:AnyRef):T =
    if ( manifest.runtimeClass == classOf[Int] ) {
      value match {
        case n:java.lang.Integer => n.asInstanceOf[T]
        case x => throw new IllegalArgumentException("can't turn " + x + " into a " + manifest.runtimeClass)
      }
    } else if ( manifest.runtimeClass  == classOf[Boolean] ) {
      value match {
        case b:java.lang.Boolean => b.asInstanceOf[T]
        case x => throw new IllegalArgumentException("can't turn " + x + " into a " + manifest.runtimeClass)
      }
    } else if ( manifest.runtimeClass.isAssignableFrom(value.getClass) ) {
      value.asInstanceOf[T]
    } else {
      throw new IllegalArgumentException("can't turn " + value + " into a " + manifest.runtimeClass)
    }

  override def getAttribute(attrName: String): AnyRef = configuration.namedVertices.get(attrName) match {
    case Some(vertex) => vertex match {
      case filter:ImmutableFilter =>
        parameterValueToJmxValue(filter.condition.asInstanceOf[ParameterizedCondition[_]].parameterValue)
      case valve:ImmutableValve =>
        parameterValueToJmxValue(valve.open)
      case x =>
        throw new IllegalStateException("unknown named type: " + x.getClass.getName + " - " + x)
    }
    case _ => throw new AttributeNotFoundException
  }

  override def setAttribute(attr: Attribute) = {
    configuration = configuration.reconfigure(attr.getName,attr.getValue)
  }

  override def getAttributes(attrNames:Array[String]): AttributeList = {
    val answer = new AttributeList()
    attrNames foreach { name =>
      answer.add(new Attribute(name,getAttribute(name)))
    }
    answer
  }

  override def setAttributes(attrs:AttributeList): AttributeList = {
    import scala.collection.JavaConversions._
    attrs.asList foreach { attr =>
      setAttribute(attr)
    }
    attrs
  }

  override def invoke(action: String, params: Array[AnyRef], signature: Array[String]): AnyRef =
    throw new IllegalArgumentException("unsupported action: " + action)

  override def getMBeanInfo = mbeanInfo match {
    case Some(i) => i
    case None =>
      val attrs = getAttributeInfos
      val i =
        new MBeanInfo(getClass.getName,
                      "timber LoggerManager",
                      attrs,
                      Array(),
                      Array(),
                      Array())
      mbeanInfo = Some(i)
      i
  }

  private def getAttributeInfos = {
    val ne = configuration.namedVertices
    log.debug("namedVertices = " + ne)
    ne.map { case (name,condition) =>
      log.debug("name:" + name + " condition:" + condition)
      val attr = condition match {
        case d:ImmutableFilter =>
          val p = d.condition.asInstanceOf[ParameterizedCondition[_]]
          val cls = p.parameterType.getName
          new MBeanAttributeInfo(name,cls,null,true,true,false)
        case v:ImmutableValve =>
          new MBeanAttributeInfo(name,"java.lang.Boolean",null,true,true,false)
        case _:ImmutableReceiver => throw new IllegalArgumentException("shouldn't be a named Receiver here")
      }
      log.debug("resulting attr = " + attr)
      attr
    }.toArray
  }

  private var mbeanInfo:Option[MBeanInfo] = None

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
