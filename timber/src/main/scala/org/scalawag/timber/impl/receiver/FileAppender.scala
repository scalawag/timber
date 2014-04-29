package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.formatter.EntryFormatter
import java.io._
import scala.Some
import org.scalawag.timber.impl.InternalLogging

class FileAppender(val file:File,
                   override val formatter:EntryFormatter,
                   val append:Boolean = true,
                   val charset:Option[String] = None) extends FormattingReceiver(formatter) with ResourceBasedReceiver with InternalLogging {
  protected def open:PrintWriter = {
    log.debug("Opening file: " + file.getPath)
    Option(file.getParentFile).map(_.mkdirs)
    charset match {
      case Some(csn) => new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,append),csn))
      case None => new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,append)))
    }
  }

  override val toString:String = """FILE("%s")""".format(file.getPath)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
