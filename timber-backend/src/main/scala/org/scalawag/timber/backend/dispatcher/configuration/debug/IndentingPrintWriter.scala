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

package org.scalawag.timber.backend.dispatcher.configuration.debug

import java.io.PrintWriter
import java.io.Writer
import java.util.Locale

// $COVERAGE-OFF$

private[debug] class IndentingPrintWriter(
    private val writer: Writer,
    private val indentWidth: Int = IndentingPrintWriter.DEFAULT_INDENT_WIDTH,
    autoFlush: Boolean = false
) extends PrintWriter(writer, autoFlush) {
  private var currentLevel = 0
  private var currentPrefix = ""
  private var needsPrefix = false

  private def beforeOutput = {
    if (this.needsPrefix) {
      out.append(currentPrefix)
      this.needsPrefix = false
    }
  }

  def indent(change: Int)(fn: => Unit): Unit = {
    changeIndent(+change)
    fn
    changeIndent(-change)
  }

  def indent(fn: => Unit): Unit = indent(1)(fn)

  def changeIndent(change: Int): Unit = {
    this.currentLevel = Iterable(0, this.currentLevel + change).max
    this.currentPrefix = " " * (indentWidth * this.currentLevel)
  }

  override def println(): Unit = {
    super.println()
    this.needsPrefix = true
  }

  override def write(c: Int): Unit = {
    beforeOutput
    super.write(c)
  }

  override def write(buf: Array[Char], off: Int, len: Int): Unit = {
    beforeOutput
    super.write(buf, off, len)
  }

  override def write(buf: Array[Char]): Unit = {
    beforeOutput
    super.write(buf)
  }

  override def write(s: String, off: Int, len: Int): Unit = {
    beforeOutput
    super.write(s, off, len)
  }

  override def write(s: String): Unit = {
    beforeOutput
    super.write(s)
  }

  override def print(b: Boolean): Unit = {
    beforeOutput
    super.print(b)
  }

  override def print(c: Char): Unit = {
    beforeOutput
    super.print(c)
  }

  override def print(i: Int): Unit = {
    beforeOutput
    super.print(i)
  }

  override def print(l: Long): Unit = {
    beforeOutput
    super.print(l)
  }

  override def print(f: Float): Unit = {
    beforeOutput
    super.print(f)
  }

  override def print(d: Double): Unit = {
    beforeOutput
    super.print(d)
  }

  override def print(s: Array[Char]): Unit = {
    beforeOutput
    super.print(s)
  }

  override def print(s: String): Unit = {
    beforeOutput
    super.print(s)
  }

  override def print(obj: Any): Unit = {
    beforeOutput
    super.print(obj)
  }

  override def println(x: Boolean): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Char): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Int): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Long): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Float): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Double): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Array[Char]): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: String): Unit = {
    beforeOutput
    super.println(x)
  }

  override def println(x: Any): Unit = {
    beforeOutput
    super.println(x)
  }

  override def printf(format: String, args: Object*): PrintWriter = {
    beforeOutput
    super.printf(format, args)
  }

  override def printf(l: Locale, format: String, args: Object*): PrintWriter = {
    beforeOutput
    super.printf(l, format, args)
  }

  override def format(format: String, args: Object*): PrintWriter = {
    beforeOutput
    super.format(format, args)
  }

  override def format(l: Locale, format: String, args: Object*): PrintWriter = {
    beforeOutput
    super.format(l, format, args)
  }

  override def append(csq: CharSequence): PrintWriter = {
    beforeOutput
    super.append(csq)
  }

  override def append(csq: CharSequence, start: Int, end: Int): PrintWriter = {
    beforeOutput
    super.append(csq, start, end)
  }

  override def append(c: Char): PrintWriter = {
    beforeOutput
    super.append(c)
  }
}

object IndentingPrintWriter {
  val DEFAULT_INDENT_WIDTH = 2
}

// $COVERAGE-ON$
