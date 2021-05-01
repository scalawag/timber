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

package com.example.logclient

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.backend.dispatcher.configuration.dsl._
import org.scalawag.timber.backend.receiver.Receiver
import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.api.{Level, Entry, BaseLogger}

class SimpleLoggingTest extends AnyFunSpec with Matchers with MockFactory {

  it("should succeed in the basic logging flow") {
    val r = mock[Receiver]

    implicit val lm = new Dispatcher {
      configure { IN =>
        IN ~> ( logger("name") startsWith "com.example.logclient" ) ~> ( level >= 2 ) ~> r
        IN ~> ( level >= 3 ) ~> r
      }
    }

    val il = new BaseLogger("name" -> "com.example.logclient.FakeClass")
    val el = new BaseLogger("name" -> "org.apache.hadoop.Something")

    (r.receive _).expects(where(matches(2,il))).once()
    (r.receive _).expects(where(matches(3,il))).once()
    (r.receive _).expects(where(matches(4,il))).once()
    (r.receive _).expects(where(matches(3,el))).once()
    (r.receive _).expects(where(matches(4,el))).once()

    Iterable(il,el).foreach { l =>
      (0 to 4) foreach { level =>
        l.log(level)("level " + level)
      }
    }

  }

  def matches(level:Level,logger:BaseLogger) = { e:Entry =>
    Some(level) == e.level && logger.attributes == e.loggerAttributes
  }
}

