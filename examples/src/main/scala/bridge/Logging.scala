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

package bridge

import org.scalawag.timber.backend.dispatcher.Dispatcher
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.bridge.slf4j.Slf4jBridgeDispatcher

object Logging {

  import org.scalawag.timber.backend.dispatcher.configuration.dsl._
  import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter
  import org.scalawag.timber.backend.receiver.formatter.ProgrammableEntryFormatter._

  object Dispatcher
      extends Dispatcher(
        Configuration(
          stderr(
            new ProgrammableEntryFormatter(
              Seq(
                entry.loggingClass map { _.reverse }
              )
            )
          )
        )
      )

  /* This tells the slf4j bridge to use this dispatcher instead of the default one.  It can be changed at any time.
   */

  def configurate {
    Slf4jBridgeDispatcher.set(Dispatcher)
  }

}
