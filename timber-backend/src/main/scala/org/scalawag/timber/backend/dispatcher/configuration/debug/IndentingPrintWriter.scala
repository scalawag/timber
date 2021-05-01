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

  def indent(change: Int)(fn: => Unit) {
    changeIndent(+change)
    fn
    changeIndent(-change)
  }

  def indent(fn: => Unit): Unit = indent(1)(fn)

  def changeIndent(change: Int) {
    this.currentLevel = Iterable(0, this.currentLevel + change).max
    this.currentPrefix = " " * (indentWidth * this.currentLevel)
  }

  override def println() {
    super.println()
    this.needsPrefix = true
  }

  override def write(c: Int) {
    beforeOutput
    super.write(c)
  }

  override def write(buf: Array[Char], off: Int, len: Int) {
    beforeOutput
    super.write(buf, off, len)
  }

  override def write(buf: Array[Char]) {
    beforeOutput
    super.write(buf)
  }

  override def write(s: String, off: Int, len: Int) {
    beforeOutput
    super.write(s, off, len)
  }

  override def write(s: String) {
    beforeOutput
    super.write(s)
  }

  override def print(b: Boolean) {
    beforeOutput
    super.print(b)
  }

  override def print(c: Char) {
    beforeOutput
    super.print(c)
  }

  override def print(i: Int) {
    beforeOutput
    super.print(i)
  }

  override def print(l: Long) {
    beforeOutput
    super.print(l)
  }

  override def print(f: Float) {
    beforeOutput
    super.print(f)
  }

  override def print(d: Double) {
    beforeOutput
    super.print(d)
  }

  override def print(s: Array[Char]) {
    beforeOutput
    super.print(s)
  }

  override def print(s: String) {
    beforeOutput
    super.print(s)
  }

  override def print(obj: Any) {
    beforeOutput
    super.print(obj)
  }

  override def println(x: Boolean) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Char) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Int) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Long) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Float) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Double) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Array[Char]) {
    beforeOutput
    super.println(x)
  }

  override def println(x: String) {
    beforeOutput
    super.println(x)
  }

  override def println(x: Any) {
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
