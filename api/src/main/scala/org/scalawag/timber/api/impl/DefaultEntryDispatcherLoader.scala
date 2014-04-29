package org.scalawag.timber.api.impl

import scala.reflect.runtime.universe
import scala.collection.JavaConversions._
import org.scalawag.timber.api.Logger

object DefaultEntryDispatcherLoader {
  private val DEFAULT_ENTRY_DISPATCHER = "org.scalawag.timber.backend.DefaultEntryDispatcher"

  lazy val dispatcher = {
    val classLoaders = Seq(
      Option(Thread.currentThread.getContextClassLoader),
      Some(classOf[Logger].getClassLoader),
      Some(ClassLoader.getSystemClassLoader)
    ).flatten

    val resourceName = DEFAULT_ENTRY_DISPATCHER.replaceAllLiterally(".","/") + "$.class"

    // Select the first ClassLoader that returns at least one DefaultEntryDispatcher object

    val classLoaderAndResources = classLoaders map { cl =>
      (cl,cl.getResources(resourceName).toSeq)
    }

    val firstClassLoaderWithMatchingResources = classLoaderAndResources find { case (cl,resources) =>
      ! resources.isEmpty
    }

    // Make sure that we found it and that there's only one of these on the classpath of the selected class loader

    firstClassLoaderWithMatchingResources match {
      case None =>
        throw new Exception("No default timber dispatcher defined: add timber.jar, timber-over-slf4j.jar or timber-over-osgi.jar (or another jar with this object) to your classpath or use Thread.setContextClassLoader.")
      case Some((classLoader,Seq(first))) =>
        val rootMirror = universe.runtimeMirror(classLoader)
        val driverSymbol = rootMirror.staticModule(DEFAULT_ENTRY_DISPATCHER)
        val driverMirror = rootMirror.reflectModule(driverSymbol)
        driverMirror.instance.asInstanceOf[EntryDispatcher]
      case Some((classLoader,all)) =>
        val locations = all.map(_.toString.replaceAllLiterally(s"!/$resourceName","")).mkString(" ")
        throw new Exception(s"Found multiple objects for $DEFAULT_ENTRY_DISPATCHER on the classpath ($locations) of ClassLoader $classLoader but expecting exactly one.")
    }
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
