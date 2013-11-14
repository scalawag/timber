package org.scalawag.timber.api.impl

import scala.reflect.runtime.universe
import scala.collection.JavaConversions._

object DefaultEntryDispatcherLoader {
  private val DEFAULT_ENTRY_DISPATCHER = "org.scalawag.timber.backend.DefaultEntryDispatcher"

  lazy val dispatcher = {
    val classLoader = Thread.currentThread.getContextClassLoader //ClassLoader.getSystemClassLoader

    // Make sure there's only one of these on the classpath

    val resourceName = DEFAULT_ENTRY_DISPATCHER.replaceAllLiterally(".","/") + "$.class"
    classLoader.getResources(resourceName).toSeq match {
      case Seq() =>
        throw new Exception("No default timber dispatcher defined: add timber.jar or timber-over-slf4j.jar (or another jar with this object) to your classpath.")
      case Seq(first) =>
        // exactly one found, carry on
      case Seq(all@_*) =>
        val locations = all.map(_.toString.replaceAllLiterally(s"!/$resourceName","")).mkString(" ")
        throw new Exception(s"Found multiple objects for $DEFAULT_ENTRY_DISPATCHER on the classpath ($locations) but expecting exactly one.")
    }

    val rootMirror = universe.runtimeMirror(classLoader)
    val driverSymbol = rootMirror.staticModule(DEFAULT_ENTRY_DISPATCHER)
    val driverMirror = rootMirror.reflectModule(driverSymbol)
    driverMirror.instance.asInstanceOf[EntryDispatcher]
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
