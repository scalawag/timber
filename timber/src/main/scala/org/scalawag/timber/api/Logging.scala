package org.scalawag.timber.api

/** This is a convenient way to expose a log member to classes that need to log and pull in the implicits that are
  * needed for normal logging patterns.  Note that implementing this trait directly necessitates implementing the
  * "loggerFactory" member.  In a typical application, you would extend this trait with an application-specific
  * subclass that defines the LoggerFactory in use by your application.
  *
  * Note that this creates a per-instance Logger field, even though all the fields should point to the same Logger.
  * If you have a ton of instances for a particular class and want to keep the memory footprint as small as possible,
  * you may want to explicitly refer to a field in a companion object (which can implement this trait).  In that case,
  * you'll probably have to import the Logging implicits explicitly.
  */

trait Logging[T <: Logger] {
  protected[this] implicit val stringFnToMessage = Message.stringFnToMessage _
  protected[this] implicit val throwableToMessage = Message.throwableToMessage _
  protected[this] implicit val messageGatherer = Message.messageGatherer _

  protected[this] val loggerFactory:LoggerFactory[T]
  protected[this] lazy val log:T = loggerFactory.getLogger(getClass)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
