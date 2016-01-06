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

package org.scalawag.timber.backend.receiver

import java.io.Writer
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.formatter.{DefaultEntryFormatter, EntryFormatter}

/** Implements a [[Receiver]] that formats entries with an [[EntryFormatter]] and then writes them to a [[Writer]].
  * The Writer is specified using a writer creation function so that this receiver can recreate the Writer as many
  * times as it needs to.  This may happen if `close()` is called before the last time `receive(Entry)` is called.
  * When `receive(Entry)` is called after `close()`, the function is used to recreate the Writer.  This functionality
  * is used to support logrotate integration (or other utilities that would need a log file to be reopened).
  *
  * @param createWriterFn a function that creates the Writer which entries should be written to
  * @param formatter the formatter used to format the entries before writing them
  */

class WriterBasedReceiver(createWriterFn: => Writer)(implicit formatter:EntryFormatter = DefaultEntryFormatter) extends Receiver {
  private[this] var writerOption:Option[Writer] = None

  private[this] def writer:Writer = writerOption getOrElse {
    val w = createWriterFn
    writerOption = Some(w)
    w
  }

  override def receive(entry: Entry): Unit = writer.write(formatter.format(entry))

  override def flush(): Unit = writerOption.foreach(_.flush())

  override def close() =
    writerOption match {
      case Some(w) =>
        w.close
        writerOption = None
      case None =>
      // noop
    }
}

/** A stackable version of the [[WriterBasedReceiver]]. */

class WriterBasedStackableReceiver(createWriterFn: => Writer)(implicit formatter:EntryFormatter = DefaultEntryFormatter)
  extends StackableReceiver(new WriterBasedReceiver(createWriterFn)(formatter))

