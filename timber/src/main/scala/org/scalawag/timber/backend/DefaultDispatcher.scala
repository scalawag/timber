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

import org.scalawag.timber.api.Entry

import scala.language.postfixOps

import org.scalawag.timber.backend.dispatcher.Dispatcher
import java.util.concurrent.atomic.AtomicReference
import org.scalawag.timber.api

/** Provides a default [[api.Dispatcher dispatcher]] to be used by any timber API loggers in the system that didn't
  * specify another dispatcher. It delegates all of the dispatching work to another dispatcher which can be specified
  * using a system property or changed at runtime.
  *
  * By default, the delegate created by this object is an instance of
  * [[org.scalawag.timber.backend.dispatcher.Dispatcher]].  This should be appropriate for most situations.  In the
  * event that you need a different type of dispatcher, there are two ways that you can determine the delegate.
  *
  * <ul>
  *   <li>
  *     If the `<b>timber.dispatcher.class</b>` system property contains the name of a class with a default constructor
  *     that extends the [[Dispatcher]] trait, the initial delegate will be a newly-created instance of that class.
  *   </li>
  *   <li>
  *     During your process bootstrap, you can use the set(Dispatcher) method to change the delegate.  In this case,
  *     a default constructor is not required, since you instantiate the object yourself.  It's not essential that this
  *     call be made prior to any logging calls, but only entries that are dispatched after the call will use the new
  *     dispatcher that you put in place.  There may be some leakage on stderr (where the default initial dispatcher
  *     writes its entries) if you don't get your dispatcher in place soon enough.
  *   </li>
  * </ul>
  */

object DefaultDispatcher extends api.Dispatcher {
  override def dispatch(entry:Entry) = dispatcherRef.get.dispatch(entry)

  /** Sets a new delegate for all the dispatch calls received by this dispatcher.  This call is thread-safe and
    * can be called any number of times during the process' lifetime.  The normal use case is for it to be set once
    * during the process bootstrap, before other logging calls are made.
    *
    * @param delegate the dispatcher which should handle all entries for timber loggers not specifying another dispatcher
    */
  def set(delegate:api.Dispatcher) = dispatcherRef.set(delegate)

  import org.scalawag.timber.backend.{InternalLogger => log}

  private val SYSTEM_PROPERTY = "timber.dispatcher.class"

  private[this] val dispatcherRef = new AtomicReference[api.Dispatcher]({
    val DEFAULT_DISPATCHER_CLASS_NAME = classOf[Dispatcher].getName

    Option(System.getProperty(SYSTEM_PROPERTY)) match {
      case Some(className) =>
        loadDispatcherByName(className) getOrElse {
          log.warning(s"Falling back to the default timber dispatcher class ($DEFAULT_DISPATCHER_CLASS_NAME)")
          new Dispatcher
        }
      case None =>
        log.debug(s"The system property '$SYSTEM_PROPERTY' is not specified, using the default dispatcher ($DEFAULT_DISPATCHER_CLASS_NAME).")
        new Dispatcher
    }
  })

  private def loadDispatcherByName(className:String) = {
    val classLoaders = Seq(
      Option(Thread.currentThread.getContextClassLoader),
      Some(this.getClass.getClassLoader),
      Some(ClassLoader.getSystemClassLoader)
    ).flatten

    // Try each of the ClassLoaders in order until we get to one that can find the specified class.

    val eitherClassOrExceptions =
      classLoaders map { cl =>
        try {
          Left(Class.forName(className, true, cl))
        } catch {
          case ex:ClassNotFoundException => Right(ex)
        }
      }

    // Choose the class that we'll use (the first one we found).  Log an error if no ClassLoader could find one.

    eitherClassOrExceptions collectFirst { case Left(cls) =>

      // Instantiate the dispatcher based on the class we chose above.

      try {
        Some(cls.newInstance().asInstanceOf[api.Dispatcher])
      } catch {
        case ex:ClassCastException =>
          log.error(s"The class specified by the system property '$SYSTEM_PROPERTY' ($className) is not a timber dispatcher (${classOf[api.Dispatcher].getName}).")
          None
        case ex:Exception =>
          log.error(s"The class specified by the system property '$SYSTEM_PROPERTY' ($className) could not be instantiated: $ex")
          None
      }

    } getOrElse {
      log.error(s"The class specified by the system property '$SYSTEM_PROPERTY' ($className) could not be found.")
      None
    }
  }
}

