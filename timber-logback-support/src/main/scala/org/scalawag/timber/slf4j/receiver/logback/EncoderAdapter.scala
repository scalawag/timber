// timber -- Copyright 2012-2026 -- Justin Patterson
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

import ch.qos.logback.core.encoder.EncoderBase
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.receiver.ConsoleReceiver
import org.scalawag.timber.backend.receiver.formatter.EntryFormatter

import java.io.{ByteArrayOutputStream, OutputStream, OutputStreamWriter, Writer}

class EncoderAdapter(private val formatter: EntryFormatter, charset: Option[String] = None) extends EncoderBase[Entry] {
  def encode(entry: Entry) = charset.fold(formatter.format(entry).getBytes())(formatter.format(entry).getBytes(_))
  override def headerBytes(): Array[Byte] = Array.empty
  override def footerBytes(): Array[Byte] = Array.empty
}
