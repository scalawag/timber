package org.scalawag.timber.api

/** This class gives you a way to create Logger classes.  Using the old patterns established by log4j, you can either
  * give each Logger a name or use the convention of naming your Loggers based on the class doing the logging.  If you
  * only want to do the latter, you can simply follow the pattern of extending the Logging trait for your application
  * and it will create a Logger per instance for you.  If that's not what you want, for example, if you want a special
  * Logger with a simple name like "ERRORS," you can create that Logger explicitly from the LoggerManager.  It's fine
  * to mix the styles.
  */

trait LoggerFactory[+T <: Logger] {
  def getLogger(name:String):T
  final def getLogger(c:Class[_]):T = getLogger(c.getName)
  final def getLogger[C:Manifest]:T = getLogger(manifest.erasure)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
