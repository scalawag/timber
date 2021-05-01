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

package org.scalawag.timber.backend.receiver.formatter

import org.scalawag.timber.api.Entry

/** Most of the declarations in this object should be used through the DSL.  See [[ProgrammableEntryFormatter]] for
  * more information.
  */
object ProgrammableEntryFormatter {
  private val newline = System.getProperty("line.separator")

  object ContinuationHeader extends Enumeration {
    val METADATA = Value
    val INDENT = Value
    val NONE = Value
  }

  object MetadataProvider {
    implicit def fromString(s: String) = new LiteralMetadata(s)
  }

  trait MetadataProvider {
    def extractFrom(entry: Entry): Option[String]
  }

  class LiteralMetadata(literal: String) extends MetadataProvider {
    override def extractFrom(entry: Entry) = Some(literal)
  }

  // "Optional" indicates a provider that may return None when asked to extractFrom and therefore can have fallbacks specified.
  // "Chain" indicates a provider that will try delegating to multiple other providers until it finds one that responds with content.
  // "Extracting" indicates a provider that actually does the work of pulling metadata from the entry.
  // "Formatted" indicates a provider with an associated formatter for the type of metadata it provides.

  trait OptionalMetadataProvider extends MetadataProvider {
    def orElse(fallback: OptionalMetadataProvider) = new OptionalChainMetadataProvider(Seq(this, fallback))
    def orElse(fallback: MetadataProvider) = new ChainMetadataProvider(Seq(this), fallback)
  }

  class OptionalChainMetadataProvider private[formatter] (
      private[formatter] val delegates: Seq[OptionalMetadataProvider]
  ) extends OptionalMetadataProvider {
    override def extractFrom(entry: Entry) = delegates.map(c => c.extractFrom(entry)).find(_.isDefined).flatten
    override def orElse(fallback: OptionalMetadataProvider) =
      new OptionalChainMetadataProvider(this.delegates :+ fallback)
    override def orElse(fallback: MetadataProvider) = new ChainMetadataProvider(this.delegates, fallback)
  }

  class ChainMetadataProvider private[formatter] (
      delegates: Seq[OptionalMetadataProvider],
      lastResort: MetadataProvider
  ) extends MetadataProvider {
    override def extractFrom(entry: Entry) =
      delegates.map(c => c.extractFrom(entry)).find(_.isDefined).flatten orElse lastResort.extractFrom(entry)
  }

  class ExtractingMetadataProvider[A] private[formatter] (extractor: Entry => A) extends MetadataProvider {
    override def extractFrom(entry: Entry) = Some(extractor(entry).toString)
    def formattedWith(formatter: Formatter[A]) = new FormattedExtractingMetadataProvider(extractor, formatter)
    def map[B](fn: A => B): ExtractingMetadataProvider[B] = new ExtractingMetadataProvider(extractor andThen fn)
  }

  class OptionalExtractingMetadataProvider[A] private[formatter] (extractor: Entry => Option[A])
      extends OptionalMetadataProvider {
    override def extractFrom(entry: Entry) = extractor(entry).map(_.toString)
    def formattedWith(formatter: Formatter[A]) =
      new FormattedOptionalExtractingMetadataProvider[A](extractor, formatter)
    def map[B](fn: A => B): OptionalExtractingMetadataProvider[B] = {
      def optFn(oa: Option[A]): Option[B] = oa.map(fn)
      new OptionalExtractingMetadataProvider(extractor andThen optFn)
    }
  }

  class MapExtractingMetadataProvider[A, B] private[formatter] (extractor: Entry => Map[A, B])
      extends ExtractingMetadataProvider[Map[A, B]](extractor) {
    def without(key: A) = this map { m: Map[A, B] => m - key }
    def without(keys: Set[A]) = this map { m: Map[A, B] => m -- keys }
    def map[C, D](fn: Map[A, B] => Map[C, D]) = new MapExtractingMetadataProvider(extractor andThen fn)
  }

  class FormattedExtractingMetadataProvider[T] private[formatter] (extractor: Entry => T, formatter: Formatter[T])
      extends MetadataProvider {
    override def extractFrom(entry: Entry) = Some(formatter.format(extractor(entry)))
  }

  class FormattedOptionalExtractingMetadataProvider[T] private[formatter] (
      extractor: Entry => Option[T],
      formatter: Formatter[T]
  ) extends OptionalMetadataProvider {
    override def extractFrom(entry: Entry) = extractor(entry).map(formatter.format)
  }

  class Delimiter private[formatter] (delimiter: String) extends Formatter[Iterable[Any]] {
    override def format(value: Iterable[Any]) = value.map(_.toString).mkString(delimiter)
  }

  object Delimiter {
    def apply(delimiter: String) = new Delimiter(delimiter)
  }

  object Commas extends Delimiter(",")

  object Spaces extends Delimiter(" ")

  object CommasAndEquals extends Formatter[Map[String, Any]] {
    override def format(value: Map[String, Any]) = value map { case (k, v) => s"$k=$v" } mkString ","
  }

  object TopsOnly extends (Map[String, List[String]] => Map[String, String]) {
    override def apply(in: Map[String, List[String]]) =
      in.flatMap {
        case (k, v) =>
          v.headOption.map(k -> _)
      }
  }

  object entry {
    val threadName = new ExtractingMetadataProvider(e => e.threadName)
    val timestamp = new ExtractingMetadataProvider(e => e.timestamp)
    val level = new OptionalExtractingMetadataProvider(_.level)
    val loggingClass = new OptionalExtractingMetadataProvider(_.loggingClass)
    val loggingMethod = new OptionalExtractingMetadataProvider(_.loggingMethod)
    val sourceLocation = new OptionalExtractingMetadataProvider(_.sourceLocation)
    val tags = new ExtractingMetadataProvider(e => e.tags)
    val loggerAttributes = new MapExtractingMetadataProvider(e => e.loggerAttributes)
    val threadAttributes = new MapExtractingMetadataProvider(e => e.threadAttributes)
    def loggerAttribute(name: String) = new OptionalExtractingMetadataProvider(e => e.loggerAttributes.get(name))
    def threadAttribute(name: String) =
      new OptionalExtractingMetadataProvider(e => e.threadAttributes.get(name).flatMap(_.headOption))
  }
}

import ProgrammableEntryFormatter._
import org.scalawag.timber.api.Entry

/** Formats [[Entry entries]] using the specified metadata providers to create a header.  The metadata providers can
  * be specified using the mini-DSL on the `entry` object:
  *   - `threadName` - the name of the thread that created the entry
  *   - `timestamp` - the timestamp at which the entry was created
  *   - `level` - the level at which the entry was created (if available)
  *   - `loggingClass` - the class from which the log method was called (if available)
  *   - `loggingMethod` - the method from which the log method was called (if available)
  *   - `sourceLocation` - the location in source (file name and line number) from which the log method was called (if available)
  *   - `tags` - the tags associated with the entry
  *   - `loggerAttributes` - the logger attributes associated with the entry
  *   - `threadAttributes` - the thread attributes associated with the entry
  *   - `loggerAttribute(name)` - the logger attribute with the specified name associated with the entry (if available)
  *   - `threadAttribute(name)` - the thread attribute with the specified name associated with the entry (if available)
  *
  * {{{
  *   new ProgrammableEntryFormatter(Seq(entry.threadName,entry.loggingClass))
  * }}}
  *
  * You can also specify Strings to be included literally.
  *
  * {{{
  *   new ProgrammableEntryFormatter(Seq("literal",entry.loggingClass))
  * }}}
  *
  * In addition to the providers above, you can specify some modifiers to tweak them before their inclusion:
  *   - `formattedWith` - allows you to choose how the metadata is formatted, must match the type of the metadata
  *   - `map` - allows you to apply an arbitrary mapping function to the metadata before inclusion
  *   - `without` - allows you to remove keys from a map before inclusion (e.g., if you already included on of the keys specifically)
  *
  * If you don't specify a formatter, the `toString` method of the object will be used. There are some built-in
  * formatters for [[org.scalawag.timber.backend.receiver.formatter.timestamp timestamps]] and
  * [[org.scalawag.timber.backend.receiver.formatter.level levels]] that you can use.  There are also some built-in
  * formatters for any Iterables that may be more appealing than the default `toString` implementation.
  *   - `Commas` - formats Iterables as strings with commas separating the items
  *   - `Spaces` - formats Iterables as strings with spaces separating the items
  *   - `Delimiter(str)` - formats Iterables as strings with the specified delimiter separating the items
  *   - `CommasAndEquals` - formats Maps as equals-separated pairs separated by commas (e.g., `k1=v1,k2=v2`)
  *
  * {{{
  *   entry.timestamp formattedWith HumanReadableTimestampFormatter
  *   entry.timestamp formattedWith SimpleDateFormatTimestampFormatter("yyyy/MM/dd")
  *   entry.threadName map { name => name.length }
  *   entry.loggerAttributes without "name" formattedWith CommasAndEquals
  * }}}
  *
  * For the metadata that may not be present (indicated with "if available" above), you can specify a fallback to use when it's absent with `orElse`.
  * You can't use `orElse` after a metadata provider that will definitely return something.
  *
  * {{{
  *   entry.loggingClass orElse entry.loggerAttribute("name") orElse "unknown"
  * }}}
  *
  * With certain combinations of modifiers, you may have to include parentheses if the scala compiler can't figure out
  * the operator precedence.  You can also specify the modifiers without utilizing the infix call style, if you prefer.
  *
  * @param metadataProviders a list of metadata providers to be used to create headers to be included with the message
  * @param delimiter the string that separates metadata headers from each other and from the message lines
  * @param continuationHeader determines what header is included on lines after the first
  * @param firstLinePrefix determines the prefix for the first line of output for an entry
  * @param continuationPrefix determines the prefix for the remaining lines of an entry
  * @param missingValueString determines the string to use for optional metadata providers that return no content
  */
class ProgrammableEntryFormatter(
    val metadataProviders: Seq[MetadataProvider],
    val delimiter: String = "|",
    val continuationHeader: ProgrammableEntryFormatter.ContinuationHeader.Value =
      ProgrammableEntryFormatter.ContinuationHeader.NONE,
    val firstLinePrefix: String = "+",
    val continuationPrefix: String = " ",
    val missingValueString: String = ""
) extends EntryFormatter {
  def format(entry: Entry): String = {
    val headerComponents = metadataProviders.map(_.extractFrom(entry)).map(_.getOrElse(missingValueString))
    val header = headerComponents.mkString("", delimiter, if (entry.message.isDefined) delimiter else "")

    val continuationHeaderString =
      continuationHeader match {
        case ContinuationHeader.METADATA => header
        case ContinuationHeader.INDENT   => " " * header.length
        case ContinuationHeader.NONE     => ""
      }

    val firstLineHeader = firstLinePrefix + header

    val otherLineHeader = newline + continuationPrefix + continuationHeaderString

    entry.message match {
      case Some(m) => m.lines.mkString(firstLineHeader, otherLineHeader, newline)
      case None    => s"$firstLineHeader$newline"
    }
  }
}
