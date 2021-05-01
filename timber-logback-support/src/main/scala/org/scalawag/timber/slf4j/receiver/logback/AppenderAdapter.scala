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

import ch.qos.logback.core.Appender
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.Receiver

class AppenderAdapter(private[logback] val appender: Appender[Entry]) extends Receiver {
  override def receive(entry: Entry): Unit = {
    appender.doAppend(entry)
  }

  override def flush(): Unit = {}

  override def close(): Unit = {
    appender.stop()
  }
}
