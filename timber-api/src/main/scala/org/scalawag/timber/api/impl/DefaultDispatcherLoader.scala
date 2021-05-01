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

package org.scalawag.timber.api.impl

import scala.reflect.runtime.universe
import org.scalawag.timber.api.{BaseLogger, Dispatcher}

import java.net.URL

/**
  * Loads the default [[Dispatcher dispatcher]] to be used for all loggers that do
  * not specify an alternate dispatcher.  The default dispatcher may be defined by any jar that provides an object
  * with the correct name (org.scalawag.timber.backend.DefaultDispatcher) of the correct type
  * ([[org.scalawag.timber.api.Dispatcher]]).  <em>This object is not provided by timber-api.jar and is loaded
  * through reflection</em>.
  *
  * This DefaultDispatcherLoader will search first the thread's context ClassLoader, then the ClassLoader that loaded
  * the timber API and, finally, the system ClassLoader.  The first one that can load the object will have the honor.
  * Once the ClassLoader has been selected, the loader ensures that exactly one object with that name is available
  * on its classpath. It is an error for more than one default dispatcher (with the name above) to exist. In this
  * case, a RuntimeException will be thrown when the timber system initializes and logging will not be possible.
  *
  * If no class loader from the above sequence can locate the default dispatcher object, a RuntimeException will be
  * thrown when the timber system initializes and logging will not be possible.
  *
  * For normal use, all that should be required is to select exactly one jar with a default timber dispatcher and
  * put it on the classpath.
  */

object DefaultDispatcherLoader {
  private val DEFAULT_DISPATCHER = "org.scalawag.timber.backend.DefaultDispatcher"

  lazy val dispatcher = {
    val classLoaders = Seq(
      Option(Thread.currentThread.getContextClassLoader),
      Some(classOf[BaseLogger].getClassLoader),
      Some(ClassLoader.getSystemClassLoader)
    ).flatten

    val resourceName = DEFAULT_DISPATCHER.replaceAllLiterally(".", "/") + "$.class"

    // Select the first ClassLoader that returns at least one DefaultDispatcher object

    val classLoaderAndResources: Seq[(ClassLoader, List[URL])] = classLoaders map { cl =>
      import scala.collection.JavaConverters._
      (cl, cl.getResources(resourceName).asScala.toList)
    }

    val firstClassLoaderWithMatchingResources = classLoaderAndResources find {
      case (_, resources) =>
        resources.nonEmpty
    }

    // Make sure that we found it and that there's only one of these on the classpath of the selected class loader

    firstClassLoaderWithMatchingResources match {
      case None =>
        throw new RuntimeException(
          s"No default timber dispatcher (${DEFAULT_DISPATCHER}) defined: add timber.jar, timber-over-slf4j.jar or timber-over-osgi.jar (or another jar with this object) to your classpath or use Thread.setContextClassLoader."
        )
      case Some((classLoader, Seq(first))) =>
        val rootMirror = universe.runtimeMirror(classLoader)
        val driverSymbol = rootMirror.staticModule(DEFAULT_DISPATCHER)
        val driverMirror = rootMirror.reflectModule(driverSymbol)
        driverMirror.instance.asInstanceOf[Dispatcher]
      case Some((classLoader, all)) =>
        val locations = all.map(_.toString.replaceAllLiterally(s"!/$resourceName", "")).mkString(" ")
        throw new RuntimeException(
          s"Found multiple default timber dispatchers ($DEFAULT_DISPATCHER) on the classpath ($locations) of ClassLoader $classLoader but expecting exactly one.  Please remove all but one from your classpath."
        )
    }
  }

}
