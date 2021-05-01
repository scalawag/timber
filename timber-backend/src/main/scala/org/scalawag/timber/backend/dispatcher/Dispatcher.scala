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

package org.scalawag.timber.backend.dispatcher

import java.util.concurrent.atomic.AtomicReference

import org.scalawag.timber.api
import org.scalawag.timber.api.Entry
import org.scalawag.timber.backend.dispatcher.Dispatcher.CacheKeyExtractor
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalawag.timber.backend.dispatcher.configuration.dsl.{MutableConditionVertex, SubgraphWithOutputs}
import org.scalawag.timber.backend.dispatcher.configuration.DefaultConfiguration

object Dispatcher {

  /** Attributes that can be specified when creating a [[CacheKeyExtractor]] using the apply() factory method. */
  object Attribute extends Enumeration {
    val LoggingClass = Value
    val Level = Value
    val ThreadName = Value
    val Tags = Value
  }

  object CacheKeyExtractor {
    def apply(attributes: Attribute.Value*) =
      new CacheKeyExtractor {
        override def extractKey(entry: Entry) = {
          def include[T](attribute: Attribute.Value, value: Option[T]): Option[T] =
            if (attributes.contains(attribute))
              value
            else
              None

          import Attribute._
          new EntryFacets(
            loggingClass = include(LoggingClass, Some(entry.loggingClass)),
            level = include(Level, Some(entry.level)),
            threadName = include(ThreadName, Some(entry.threadName)),
            tags = include(Tags, Some(entry.tags))
          )
        }
      }
  }

  /** Specifies the key to be used for caching configurations within a [[Dispatcher]].  If a [[Dispatcher]]
    * has a CacheKeyExtractor, it will extract the cache key (an [[EntryFacets]]) and use it to constrain
    * the configuration and store it keyed off that [[EntryFacets]].  The next time another entry is logged with the
    * same extracted cache key, it will use the preconstrained configuration instead of the full configuration.  You
    * can use this to improve the performance of your dispatcher if you know the approximate distribution of entries
    * it will be asked to dispatch.
    *
    * If you are not concerned with performance, it is safe to ignore the CacheKeyExtractor entirely.
    */
  trait CacheKeyExtractor {

    /** Extract the cache key (an EntryFacets representing the significant portions of the entry) from a given entry.
      *
      * @param entry the entry whose cache key should be extracted
      * @return the cache key
      */
    def extractKey(entry: Entry): EntryFacets
  }
}

/** The only [[api.Dispatcher Dispatcher]] provided by the timber backend.  It should handle most of your dispatching
  * needs.  Instances of this class are thread-safe.  You can change the configuration at any time.
  *
  * Until the first setConfiguration() call, dispatching is done using the initial configuration specified in the
  * constructor.  If you don't specify a configuration, the [[DefaultConfiguration]] is used.
  *
  * If you are concerned about the performance of your dispatcher, you should look into specifying a cacheKeyExtractor
  * to the constructor.  If you're not concerned about performance, you can safely ignore it.
  *
  * @param initialConfiguration the configuration used until another call to setConfiguration()
  * @param cacheKeyExtractor the optional CacheKeyExtractor for caching constrained configurations (None disables caching)
  */
class Dispatcher(
    initialConfiguration: Configuration = DefaultConfiguration,
    cacheKeyExtractor: Option[CacheKeyExtractor] = None
) extends api.Dispatcher {
  private[this] val activeConfiguration = new AtomicReference(initialConfiguration)
  private[this] val configurationCache = new AtomicReference(Map.empty[EntryFacets, Configuration])

  /** Sets the configuration for this dispatcher.  This call is safe to call without concurrency protection. Entries
    * dispatched before the call will use the old configuration and entries dispatched after the call will be
    * dispatched using the new configuration.
    *
    * @param configuration the new configuration to be used by this dispatcher
    */
  def setConfiguration(configuration: Configuration) = {
    activeConfiguration.set(configuration)
    configurationCache.set(Map.empty)
  }

  /** Dispatch an entry to the appropriate destination(s).
    *
    * @param entry the entry to be dispatched.
    */
  override def dispatch(entry: Entry) =
    getConstrainedConfigurationFor(entry).findReceivers(entry).foreach(_.receive(entry))

  private[this] def getConstrainedConfigurationFor(entry: Entry) =
    cacheKeyExtractor match {
      case None =>
        activeConfiguration.get
      case Some(extractor) =>
        // Determine the key that we'll use to cache the constrained configuration
        val key = extractor.extractKey(entry)
        // Get an immutable snapshot of the cache
        val cache = configurationCache.get
        // If there's already a constrained configuration for this key, use it.  Otherwise, create it now.
        cache.get(key) getOrElse {
          val constrained = activeConfiguration.get.constrain(key)
          // Only add the value to the cache if no one else has modified the cache in the mean time.  If that happens,
          // it means that we'll have to constrain it again next time, but we avoid retries and locks that way.
          configurationCache.compareAndSet(cache, cache + (key -> constrained))
          constrained
        }
    }

  /** Enables configuring this dispatcher using a thunk which is passed the root of the configuration.
    *
    * @param fn the thunk which takes the root vertex of the configuration and adds routes to receivers
    */

  def configure(fn: SubgraphWithOutputs[MutableConditionVertex] => Unit) {
    val IN: SubgraphWithOutputs[MutableConditionVertex] = true
    fn(IN)
    setConfiguration(IN) // Call the external API so that it can be made thread-safe, however that's done
  }
}
