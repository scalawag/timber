package org.scalawag.timber.impl

import org.scalawag.timber.api._
import java.util.concurrent.atomic.AtomicReference
import org.scalawag.timber.api.impl.Entry

object Location {
  private var loggerClasses = new AtomicReference[Map[String, Boolean]](Map())

  private def isLogger(className:String):Boolean = loggerClasses.get.get(className) match {
    case Some(b) =>
      b
    case None =>
      val b = try {
        val cls = Thread.currentThread.getContextClassLoader.loadClass(className)
        classOf[Logger].isAssignableFrom(cls)
      } catch {
        case _:ClassNotFoundException => false
      }
      loggerClasses.set(loggerClasses.get + (className -> b))
      b
  }

  private def isNotLogger(ste:StackTraceElement):Boolean = !isLogger(ste.getClassName)

  private[impl] def fromStack = {
    val stack = Thread.currentThread.getStackTrace
    val frame = stack.reverse.takeWhile(isNotLogger).lastOption
    frame.map( ste => Entry.Location(ste.getFileName,ste.getLineNumber) )
  }
}

// When a LoggerImpl mixes in this trait, it causes the Logger to insert Location information into the Entries
// produced.  Normally, this is not included because it requires a bit of extra processing, slowing things down.

trait Location extends Logger {
  abstract override def buildEntry(level:Level,message:Message,tags:Set[Tag]) =
    super.buildEntry(level,message,tags).copy(location = Location.fromStack)
}

object Locationable {
  object WithLocation extends Tag {
    override val toString = "WithLocation"
  }
}

// When this trait is mixed in, the Logger will insert the Location information for any logging call that includes
// the WithLocation tag.  All other entries will not include Location information.  This is done for performance
// reasons, when only certain entries need to include Location information.

trait Locationable extends Logger {
  import org.scalawag.timber.impl.Locationable._

  abstract override def buildEntry(level:Level,message:Message,tags:Set[Tag]) =
    if ( tags.contains(WithLocation) )
      super.buildEntry(level,message,tags).copy(location = Location.fromStack)
    else
      super.buildEntry(level,message,tags)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
