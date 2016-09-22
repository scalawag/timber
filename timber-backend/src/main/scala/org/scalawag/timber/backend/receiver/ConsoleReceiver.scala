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

import java.io.OutputStream
import java.nio.charset.Charset
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.formatter.EntryFormatter

// TODO: document not to try to use this for OutputStreams.  You should just use the WriterBasedReceiver and
// create a writer using OutputStreamWriter.  timber really deals only in writers (characters).
// These are specifically for writing to the scala console.

// This class doesn't use an OutputStreamWriter because when you create one, it locks in the underlying OutputStream.
// Since we're trying to work with scala's Console (which allows you to redirect its out and err streams, we need to
// be able to follow those changes.  It seems like the easiest thing to do it recreate the one OutputStreamWriter
// method that we'd be using (write(String)) by encoding the string manually.

private[timber] abstract class ConsoleReceiver(formatter:EntryFormatter, charsetName:Option[String] = None) extends Receiver {
  // Override to provide the stream to write to
  protected def stream:OutputStream

  private[this] val charset = charsetName match {
    case Some(csn) => Charset.forName(csn)
    case None => Charset.defaultCharset
  }

  override def receive(entry: Entry) = {
    val s = formatter.format(entry)
    val bb = charset.encode(s)
    stream.write(bb.array,bb.arrayOffset,bb.limit)
  }

  override def flush(): Unit = stream.flush()

  override def close(): Unit = stream.flush() // Don't close console streams!
}

/** A [[StackableReceiver]] that formats entries and writes them to scala's
  * [[scala.Console Console.out]] (which normally points to stdout but can be redirected).
  *
  * @param formatter the formatter to use to format the entries
  * @param charset the optional charset to use for encoding the entry text (defaults to the process default)
  */
class ConsoleOutReceiver(formatter: EntryFormatter, charset: Option[String] = None)
  extends StackableReceiver(new ConsoleReceiver(formatter,charset) {
    // Console.err is a def, so this needs to be a def as well to follow any changes to the former.
    override def stream = Console.out
  })
{
  override val toString = "Console.out"
}

/** A [[StackableReceiver]] that formats entries and writes them to scala's
  * [[scala.Console Console.err]] (which normally points to stderr but can be redirected).
  *
  * @param formatter the formatter to use to format the entries
  * @param charset the optional charset to use for encoding the entry text (defaults to the process default)
  */
class ConsoleErrReceiver(formatter: EntryFormatter, charset: Option[String] = None)
  extends StackableReceiver(new ConsoleReceiver(formatter,charset) {
    // Console.err is a def, so this needs to be a def as well to follow any changes to the former.
    override def stream = Console.err
  })
{
  override val toString = "Console.err"
}

