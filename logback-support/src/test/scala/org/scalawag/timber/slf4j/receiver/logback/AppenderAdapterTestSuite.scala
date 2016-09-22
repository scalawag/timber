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

package org.scalawag.timber.slf4j.receiver.logback

import org.scalatest.FunSuite
import org.scalawag.timber.api.{Entry, BaseLogger}
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.receiver.Receiver
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.formatter.DefaultEntryFormatter

import org.scalawag.timber.slf4j.receiver.logback
import ch.qos.logback.core.FileAppender

class AppenderAdapterTestSuite extends FunSuite  {
  import LogbackSupport._

  implicit private val dispatcher = new Dispatcher
  implicit val formatter = DefaultEntryFormatter

  test("test file") {
    dispatcher.configure { IN =>
      withLogbackSupport { implicit context =>
        val file = new AppenderAdapter(logback.file("/tmp/blah"))
        Receiver.closeOnShutdown(file)
        IN ~> file
      }
    }

    val log = new BaseLogger
    log.log(1)("log message")
  }

  test("test rolling file") {
    dispatcher.configure { IN =>
      withLogbackSupport { implicit context =>
        val rollingPolicy = logback.timeBasedRollingPolicy("/tmp/blahr-%d{yyyy-MM-dd-HH-mm-ss}.log")
        val file = new AppenderAdapter(logback.rollingFile("/tmp/blahr",rollingPolicy))
        Receiver.closeOnShutdown(file)
        IN ~> file
      }
    }

    val log = new BaseLogger
    (1 to 5) foreach { n =>
      log.log(1)("log message " + n)
      Thread.sleep(1000)
    }
  }

  test("test any logback ") {
    dispatcher.configure { IN =>
      withLogbackSupport { implicit context =>
        val encoder = new EncoderAdapter(DefaultEntryFormatter)
        encoder.setContext(context)
        context.add(encoder)

        val appender = new FileAppender[Entry]
        appender.setContext(context)
        appender.setFile("/tmp/test.log")
        appender.setEncoder(encoder)
        appender.setName("test")
        context.add(appender)

        val file = new AppenderAdapter(appender)
        Receiver.closeOnShutdown(file)
        IN ~> file
      }
    }

    val log = new BaseLogger
    log.log(1)("log message")
  }
}

