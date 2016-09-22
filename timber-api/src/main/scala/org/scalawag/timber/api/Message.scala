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

import scala.io.Source
import java.io.{StringWriter, PrintWriter}

/** Represents the textual content of an [[Entry entry]].  You normally won't create a
  * message object explicitly but will use one of the implicit conversions in the companion object (see the
  * [[http://spray.io/blog/2012-12-13-the-magnet-pattern/ magnet pattern]]).
You don't normally need to create instances of this class
  * directly but can use the implicits defined in the Logging object.
  */

class Message(fn: => String) {
  /** The full text of this message.  This may be more than one line of text, separated by newlines.  During normal
    * operation of the logging system, message text will not be calculated until it is needed. This may be when it
    * is eventually written to a log file or when the text is needed to determine its destination.
    *
    * You will probably never need to call this method yourself.  Timber will call it when it needs the message text.
    */
  lazy val text:String = fn

  /** Each String in the Traversable represents a single line in the message.  While the eventual receiver(s) of this
    * message will ultimately determine how the message is rendered, it is customary to separate the "lines" of the
    * message with newlines (hence the name).
    *
    * You will probably never need to call this method yourself.  Timber will call it when it needs the message lines.
    */
  lazy val lines:Traversable[String] = Source.fromString(text).getLines.toIterable
}

/** Contains some useful implicit conversions to Message.  These are all that make using the timber API bearable.
  * They should be in scope by virtue of being members of the companion object of the target class.
  */

object Message {
  /** Converts a String into a Message containing only the string. */
  implicit def stringFnToMessage(s: => String):Message = new Message(s)

  /** Converts a Throwable to a Message containing its stack trace. */
  implicit def throwableToMessage(t: Throwable):Message = messageGathererToMessage(t.printStackTrace)

  /** Converts a String and a Throwable to a Message containing first the String and then the stack trace of the
    * Throwable, separated by a new line.  This conversion makes any function call that requires a single Message
    * argument to appear to support two arguments.
    */
  implicit def stringAndThrowableToMessage(st: (String,Throwable)):Message =
    messageGathererToMessage { pw:PrintWriter =>
      pw.println(st._1)
      st._2.printStackTrace(pw)
    }

  /** Converts a function that takes a PrintWriter (and returns Unit) into a message containing everything written to
    * the PrintWriter during the function's execution. This can be useful when you want to write several lines of
    * text to the log and ensure that they remain together (not broken up by another entry or a file boundary).
    *
    * So, instead of this:
    * {{{
    *   val lines = Iterable[String]
    *   lines.foreach(log.debug)
    * }}}
    *
    * do this:
    * {{{
    *   val lines = Iterable[String]
    *   log.debug { pw:PrintWriter =>
    *     lines.foreach(pw.println)
    *   }
    * }}}
    */
  implicit def messageGathererToMessage(fn: PrintWriter => Unit):Message = stringFnToMessage {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    fn(pw)
    pw.close
    sw.toString
  }
}

