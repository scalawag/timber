package org.scalawag.timber.slf4s

import java.io.{StringReader, BufferedReader, StringWriter, PrintWriter}

/** Message represents the textual content of the log entry.  Each String represents a line in the message.  While
  * the EventFormatter will ultimately decide how the message is rendered, it is customary to separate the "lines"
  * of the Message with line breaks (hence the name).  You don't normally need to create instances of this class
  * directly but can use the implicits defined in the Logging object.
  */

class Message(fn: => String) {
  lazy val text:String = fn
  lazy val lines:Traversable[String] = Message.getLines(text)
}

/** Message contains some "static" functions that are useful to consumers of the API.  Normally, they'll be mixed
  * into consumer classes through extending the Logging trait below.  These are available in case you need to get
  * to the implicit conversions outside of Logging (trait) subclass.
  */

object Message {
  implicit def stringFnToMessage(s: => String):Message = new Message(s)

  implicit def throwableToMessage(t: Throwable):Message = messageGatherer(t.printStackTrace)

  implicit def messageGatherer(fn: PrintWriter => Unit):Message = stringFnToMessage {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    fn(pw)
    pw.close
    sw.toString
  }

  // Given a string that contains newlines, breaks it into a Seq[String] where each String contains a single line
  // from the original.
  //
  // I assumed that the performance would suck here but I ran some timings and it's not *that* bad.
  // This takes ~38ms per call to process a decent sized stack trace produced with printStackTrace.
  // For comparison using getLinesRegex (below) takes ~90ms.

  private def getLines(text: String): Seq[String] = {
    val br = new BufferedReader(new StringReader(text))
    val lines = scala.collection.mutable.Buffer[String]()
    var line = br.readLine
    while ( line != null ) {
      lines += line
      line = br.readLine
    }
    lines.toSeq
  }

  // Doing it with a regular expression like this turns out to be slower than using a BufferedReader (above).  Who knew?
  private val lineBreakRegex = "(\\n|\\r(\\n)?)".r
  private def getLinesRegex(text: String): Seq[String] = lineBreakRegex.split(text).toSeq
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
