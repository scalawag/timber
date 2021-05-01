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

package org.scalawag.timber.backend

import java.io.OutputStream
import org.scalawag.timber.api
import org.scalawag.timber.api.{Level, Dispatcher, Entry}
import org.scalawag.timber.api.level._
import org.scalawag.timber.backend.receiver._
import org.scalawag.timber.backend.receiver.buffering.ImmediateFlushing
import org.scalawag.timber.backend.receiver.concurrency.Locking
import org.scalawag.timber.backend.receiver.formatter.level.NameLevelFormatter
import org.scalawag.timber.backend.receiver.formatter.{
  ProgrammableEntryFormatter,
  EntryFormatter,
  DefaultEntryFormatter
}

/** The simple [[Dispatcher]] that timber uses internally for its own logging. */

private[timber] object InternalLogging extends Dispatcher {
  private[this] val debug = Option(System.getProperty("timber.debug")).isDefined

  private[this] val threshold =
    if (debug)
      Level.DEBUG
    else
      Level.WARN

  private[this] val formatter = {
    import ProgrammableEntryFormatter._
    if (debug)
      DefaultEntryFormatter
    else
      new ProgrammableEntryFormatter(
        metadataProviders = Seq("timber", entry.level formattedWith NameLevelFormatter),
        delimiter = ": ",
        continuationHeader = ContinuationHeader.METADATA,
        firstLinePrefix = "",
        continuationPrefix = ""
      )
  }

  // This is used internally for testing purposes.  We need to be able to redirect the logging so that we can capture
  // and validate it.
  private[timber] var outputStreamOverride: Option[OutputStream] = None

  class RedirectableConsoleErrReceiver(formatter: EntryFormatter, charset: Option[String] = None)
      extends ConsoleReceiver(formatter, charset) {
    override def stream = outputStreamOverride.getOrElse(Console.err)
    override val toString = "Console.err"
  }

  private[this] val receiver = Locking(ImmediateFlushing(new RedirectableConsoleErrReceiver(formatter)))

  // This is the configuration that's always used by timber internally.  It always writes to stderr.  It will
  // limit the printed log entries to those with intValue WARN or above unless the system property "timber.debug"
  // is set.  If that's the case, it will log everything.
  //
  // It's important that the building of this configuration not attempt to use any logging.  That's why I'm writing
  // a simple dispatch method instead of using the configuration DSL here.

  override def dispatch(entry: Entry): Unit = {
    if (entry.level.exists(_ >= threshold))
      receiver.receive(entry)
  }
}

/** The type of Logger that timber uses internally. */

private[timber] object InternalLogger extends api.BaseLogger()(InternalLogging) with Debug with Warning with Error
