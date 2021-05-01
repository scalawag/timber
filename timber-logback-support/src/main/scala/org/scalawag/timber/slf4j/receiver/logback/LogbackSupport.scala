// timber -- Copyright 2012-2021 -- Justin Patterson
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

package org.scalawag.timber.slf4j.receiver.logback

import ch.qos.logback.core.{BasicStatusManager, ContextBase}
import ch.qos.logback.core.status.Status
import java.io.PrintWriter
import ch.qos.logback.core.spi.LifeCycle
import org.scalawag.timber.api.style.slf4j
import org.scalawag.timber.backend.InternalLogger

object LogbackSupport {
  import scala.collection.JavaConverters._

  def withLogbackSupport[A](fn: (LogbackContext) => A): A = {
    val context = new LogbackContext
    context.setStatusManager(new LogbackStatusManager)
    val rc = fn(context)

    // Start everything that was created

    context.lifeCycles.foreach(_.start)

    // Check to see if logback is trying to tell us anything.  If so, pass it on.

    val stati = context.getStatusManager.getCopyOfStatusList.asScala.toList
    val highestLevel = (stati.map(_.getLevel) :+ 0).max

    if (highestLevel == Status.ERROR)
      throw new IllegalArgumentException("invalid logback configuration, see stderr")

    rc
  }
}

class LogbackContext extends ContextBase {
  private[logback] var lifeCycles = Set[LifeCycle]()

  def add(lifeCycle: LifeCycle) {
    lifeCycles += lifeCycle
  }
}

class LogbackStatusManager extends BasicStatusManager {

  private val levelMap = Map(
    Status.INFO -> slf4j.Level.DEBUG,
    Status.WARN -> slf4j.Level.WARN,
    Status.ERROR -> slf4j.Level.ERROR
  )

  override def add(status: Status) {
    super.add(status)

    InternalLogger.log(levelMap(status.getLevel)) { pw: PrintWriter =>
      pw.print(status.getMessage)
      Option(status.getThrowable).foreach(_.printStackTrace(pw))
    }
  }
}
