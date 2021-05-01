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

package org.scalawag.timber.api

import scala.language.experimental.macros

import scala.annotation.tailrec
import scala.reflect.macros.blackbox.Context
import org.scalawag.timber.api.BaseLogger.LogCallLocation
import Entry.SourceLocation

/** Embodies the main interface into timber.  Instances of this class are very simple objects that are essentially
  * [[Entry entry]] factories.  They are used to create entries and pass them to a [[Dispatcher dispatcher]] for
  * handling, after gathering some additional contextual information. Every BaseLogger has its associated dispatcher
  * established at construction-time. This dispatcher dispatches all of the entries that this logger creates
  * throughout its lifetime, since the dispatcher can not be changed after construction.
  *
  * Instances of this class are lightweight, immutable and thread-safe.  You should not go to great lengths to avoid
  * creating loggers.
  *
  * In addition to gathering contextual information, BaseLoggers can be given attributes and tags that will be applied
  * to all the entries they create.  Given that they are lightweight, this gives you a way to associated some shared
  * statically-scoped attributes with certain entries across threads, where the thread executing the code is not as
  * important as the static attributes.  This can be particularly useful when using scala Futures or a similar
  * technology where threads become more fungible.  Here is an example where the thread context (which is normally how
  * you would associate the same data with several log calls) becomes meaningless.
  *
  * {{{
  *   import scala.util._
  *   import scala.concurrent.Future
  *   import org.scalawag.timber.api._
  *   import scala.concurrent.ExecutionContext.Implicits.global
  *
  *   def handleRequest(clientIp:String) {
  *     val logger = new Logger(Map("clientIp" -> clientIp))
  *     logger.debug("about to start handling request")
  *     Future {
  *       logger.debug("handling request")
  *     } onComplete {
  *       case Success(_) =>
  *         logger.debug("request handled")
  *       case Failure(ex) =>
  *         logger.debug(s"failed to handle request: $$ex")
  *     }
  *   }
  * }}}
  *
  * You could also pass the static context (`clientIp`) to each log call individually, but the style shown here is a
  * little cleaner and less error-prone.
  *
  * BaseLoggers don't support the `isEnabled` type methods provided by some other logging systems.  That's because the
  * logger doesn't make any decisions regarding if (or where) the entries it creates are actually processed in any way.
  * You don't need to protect against message generation overhead because [[Message messages]] are lazily built
  * (unless you specify the [[ImmediateMessage]] tag). Anything else that you would do in application code that
  * depends on the status of the logging system is probably a bad idea.
  *
  * @param attributes the logger attributes that this logger associates with the entries it creates
  * @param tags the tags that this logger associates with the entries it creates
  * @param dispatcher the dispatcher that is used to dispatch the entries that this logger creates
  */

class BaseLogger(val attributes: Map[String, Any] = Map.empty, val tags: Set[Tag] = Set.empty)(implicit
    dispatcher: Dispatcher
) {

  def this(attributes: (String, Any)*)(implicit dispatcher: Dispatcher) = this(attributes.toMap)(dispatcher)

  /** Submits an entry with a level, a message and maybe tags to the logging system.
    *
    * The `message` argument appears in an argument list by itself because this allows us to use a thunk or a tuple
    * as the source of the implicit Message conversion.  When it appears with other arguments, the appearance of the
    * calling syntax goes downhill fast (e.g., having to wrap the thunk in parentheses or having to include extra
    * parentheses around tuples).  Similarly, `tags` is not a varargs parameter because the compiler prefers to
    * perceive multiple arguments as possible Tags (and failing) rather than look for an implicit conversion from the
    * tuple.
    *
    * @param level the level to use for the entry created
    * @param tags additional tags to include with the entry
    * @param message the message to include with the entry
    * @param location the source location of the method call (usually automatically fulfilled by LogCallLocation.capture())
    */

  def log(level: Level, tags: TraversableOnce[Tag] = Iterable.empty)(
      message: Message
  )(implicit location: LogCallLocation): Unit =
    dispatcher.dispatch(buildEntry(Some(level), Some(message), Some(location), tags))

  /** Submits an entry with a Message and tags but no level to the logging system.
    *
    * The `message` argument appears in an argument list by itself because this allows us to use a thunk or a tuple
    * as the source of the implicit Message conversion.  When it appears with other arguments, the appearance of the
    * calling syntax goes downhill fast (e.g., having to wrap the thunk in parentheses or having to include extra
    * parentheses around tuples).  Similarly, `tags` is not a varargs parameter because the compiler prefers to
    * perceive multiple arguments as possible Tags (and failing) rather than look for an implicit conversion from the
    * tuple.
    *
    * @param tags additional tags to include with the entry
    * @param message the message to include with the entry
    * @param location the source location of the method call (usually automatically fulfilled by LogCallLocation.capture())
    */

  def log(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    dispatcher.dispatch(buildEntry(None, Some(message), Some(location), tags))

  /** Submits an entry with a Message and level but no tags to the logging system.
    *
    * The `message` argument appears in an argument list by itself because this allows us to use a thunk or a tuple
    * as the source of the implicit Message conversion.  When it appears with other arguments, the appearance of the
    * calling syntax goes downhill fast (e.g., having to wrap the thunk in parentheses or having to include extra
    * parentheses around tuples).  Similarly, `tags` is not a varargs parameter because the compiler prefers to
    * perceive multiple arguments as possible Tags (and failing) rather than look for an implicit conversion from the
    * tuple.
    *
    * @param level the level to use for the entry created
    * @param message the message to include with the entry
    * @param location the source location of the method call (usually automatically fulfilled by LogCallLocation.capture())
    */

  def log(level: Level)(message: Message)(implicit location: LogCallLocation): Unit =
    dispatcher.dispatch(buildEntry(Some(level), Some(message), Some(location), Set.empty))

  /** Submits an entry with a Message but no level or tags to the logging system.
    *
    * @param message the message to include with the entry
    * @param location the source location of the method call (usually automatically fulfilled by LogCallLocation.capture())
    */

  def log(message: Message)(implicit location: LogCallLocation): Unit = {
    dispatcher.dispatch(buildEntry(None, Some(message), Some(location), Set.empty))
  }

  /** Submits an entry without a level, a Message or tags to the logging system.  This essentially creates an entry
    * with nothing but automatically-collected call metadata.
    *
    * @param location the source location of the method call (usually automatically fulfilled by LogCallLocation.capture())
    */

  def log(implicit location: LogCallLocation) = dispatcher.dispatch(buildEntry(None, None, Some(location), Set.empty))

  protected def buildEntry(
      level: Option[Level],
      message: Option[Message],
      location: Option[LogCallLocation],
      entryTags: TraversableOnce[Tag]
  ) = {
    val tags = this.tags ++ entryTags
    if (tags.contains(ImmediateMessage))
      message.foreach(_.text) // trigger message evaluation now

    Entry(
      level = level,
      message = message,
      sourceLocation = location.map(_.sourceLocation),
      loggingClass = location.flatMap(_.className),
      loggingMethod = location.flatMap(_.methodName),
      tags = tags,
      loggerAttributes = this.attributes,
      threadAttributes = ThreadAttributes.get
    )
  }
}

object BaseLogger {

  /** Represents all automatically collected information regarding the location of a log method call.  This class
    * exists so that the log method calls contain only one implicit parameter and that it's unique enough that a call
    * to the implicit [[LogCallLocation]].capture() method will be generated to fulfill the argument.
    *
    * When the entry is created, the information contained in this class is broken up into several
    * [[Entry entry]] fields (sourceLocation, loggingClass and loggingMethod).
    *
    * @param sourceLocation the source file and line number of the logging method call
    * @param className the (optional) class name from which the call was made (reflects the outermost class in the case
    *                  of local class definitions)
    * @param methodName the (optional) method name from which the call was made (reflects the method name on the class
    *                   specified by className if such a method exists)
    */

  case class LogCallLocation(
      sourceLocation: SourceLocation,
      className: Option[String] = None,
      methodName: Option[String] = None
  ) {
    override def toString = s"$sourceLocation:$className:$methodName"
  }

  object LogCallLocation {

    /** Implicitly fulfills the implicit LogCallLocation argument to all of the timber API log method calls.  This
      * method is implemented as a macro that automatically grabs source code metadata at compile time. There is no
      * run-time overhead to automatically gathering this metadata.
      *
      * @return as much source location metadata as is available
      */

    implicit def capture: LogCallLocation = macro LogCallLocation.captureImpl

    def captureImpl(c: Context): c.Expr[LogCallLocation] = {
      import c.universe._

      @tailrec
      def enclosingSymbols(sym: Symbol, path: List[Symbol] = Nil): List[Symbol] =
        if (sym == NoSymbol) {
          path
        } else {
          enclosingSymbols(sym.owner, sym :: path)
        }

      // TODO: There's more than could be done here in terms of locally-defined classes and methods.  I'm not sure it's
      // TODO: worth it, though, given that it will make conditions more complicated.
      val owners = enclosingSymbols(c.internal.enclosingOwner)

      val classNameParts = owners.tail.takeWhile(s => s.isPackage || s.isModule || s.isClass).map(_.name.toString)

      val className =
        if (classNameParts.isEmpty)
          None
        else
          Some(classNameParts.mkString("."))

      val methodName = owners.find(_.isMethod).map(_.name.toString)

      val source = Literal(Constant(c.enclosingPosition.source.file.name))
      val line = Literal(Constant(c.enclosingPosition.line))
      val loc = typeOf[LogCallLocation]
      val sloc = typeOf[SourceLocation]
      c.Expr(q"new $loc(new $sloc($source,$line),$className,$methodName)")
    }
  }

}
