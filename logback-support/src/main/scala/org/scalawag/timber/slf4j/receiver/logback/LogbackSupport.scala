package org.scalawag.timber.slf4j.receiver.logback

import org.scalawag.timber.impl.InternalLogging
import ch.qos.logback.core.{BasicStatusManager, ContextBase}
import ch.qos.logback.core.status.{StatusManager, Status}
import java.io.PrintWriter
import ch.qos.logback.core.spi.LifeCycle
import org.scalawag.timber.api.style.slf4j

object LogbackSupport extends InternalLogging {
  import scala.collection.JavaConversions._

  def withLogbackSupport[A](fn:(LogbackContext) => A):A = {
    val context = new LogbackContext
    context.setStatusManager(new LogbackStatusManager)
    val rc = fn(context)

    // Start everything that was created

    context.lifeCycles.foreach(_.start)

    // Check to see if logback is trying to tell us anything.  If so, pass it on.

    val stati = context.getStatusManager.getCopyOfStatusList.toList
    val highestLevel = ( stati.map(_.getLevel) :+ 0 ).max

    if ( highestLevel == Status.ERROR )
      throw new IllegalArgumentException("invalid logback configuration, see stderr")

    rc
  }
}

class LogbackContext extends ContextBase {
  private[logback] var lifeCycles = Set[LifeCycle]()

  def add(lifeCycle:LifeCycle) {
    lifeCycles += lifeCycle
  }
}

class LogbackStatusManager extends BasicStatusManager with InternalLogging {

  private val levelMap = Map(
    Status.INFO  -> slf4j.Level.DEBUG,
    Status.WARN  -> slf4j.Level.WARN,
    Status.ERROR -> slf4j.Level.ERROR
  )

  override def add(status: Status) {
    super.add(status)

    log.log(levelMap(status.getLevel)) { pw:PrintWriter =>
      pw.print(status.getMessage)
      Option(status.getThrowable).foreach(_.printStackTrace(pw))
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
