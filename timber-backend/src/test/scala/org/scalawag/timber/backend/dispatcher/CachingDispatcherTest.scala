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

package org.scalawag.timber.backend.dispatcher

import language.reflectiveCalls

import org.scalamock.scalatest.MockFactory
import org.scalawag.timber.backend.dispatcher.{Dispatcher => BackendDispatcher}
import org.scalawag.timber.backend.dispatcher.Dispatcher.{Attribute, CacheKeyExtractor}
import org.scalawag.timber.backend.dispatcher.configuration.Configuration
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalawag.timber.api._

class CachingDispatcherTest extends AnyFunSpec with Matchers with MockFactory {
  private class MockableConfiguration extends Configuration(Set.empty)

  describe("CacheKeyExtractor use") {

    it("should use the dispatcher's configuration directly with no CacheKeyExtractor") {
      val cfg = mock[MockableConfiguration]("cfg")
      val dispatcher = new BackendDispatcher(cfg)
      val entry = Entry(level = Some(0), loggingClass = Some("foo"))
      val entryFacets = EntryFacets(entry)

      inSequence {
        (cfg.findReceivers _).expects(entry).returns(Set.empty).once
        (cfg.findReceivers _).expects(entry).returns(Set.empty).once
      }

      dispatcher.dispatch(entry) // Should trigger a call to findReceivers
      dispatcher.dispatch(entry) // Should trigger another call to findReceivers (even though it's the same entry)
    }

    it("should create a new constrained configuration for each dispatch with a bad CacheKeyExtractor") {
      val cfg = mock[MockableConfiguration]("cfg")
      val extractor = new CacheKeyExtractor {
        override def extractKey(entry: Entry) = EntryFacets(entry) // Returns the whole entry as the key!
      }
      val dispatcher = new BackendDispatcher(cfg, Some(extractor))
      val e1 = Entry(level = Some(0), loggingClass = Some("foo"))
      val pe1 = EntryFacets(e1)
      val cc1 = mock[MockableConfiguration]("constrained")
      val e2 = Entry(level = Some(1), loggingClass = Some("foo"))
      val pe2 = EntryFacets(e2)
      val cc2 = mock[MockableConfiguration]("constrained")

      inSequence {
        (cfg.constrain _).expects(pe1, false).returns(cc1).once
        (cc1.findReceivers _).expects(e1).returns(Set.empty).once
        (cfg.constrain _).expects(pe2, false).returns(cc2).once
        (cc2.findReceivers _).expects(e2).returns(Set.empty).once
      }

      dispatcher.dispatch(e1) // Should trigger a call to constrain and findReceivers
      dispatcher.dispatch(
        e2
      ) // Should trigger another call to constrain and findReceivers (because the key is different)
    }

    it("should reuse matching constrained configurations with a good CacheKeyExtractor") {
      val cfg = mock[MockableConfiguration]("cfg")
      val extractor = new CacheKeyExtractor {
        override def extractKey(entry: Entry) =
          EntryFacets(loggingClass = Some(entry.loggingClass), level = Some(entry.level))
      }
      val dispatcher = new BackendDispatcher(cfg, Some(extractor))

      val k1 = EntryFacets(level = Some(Some(0)), loggingClass = Some(Some("foo")))
      val e1a = Entry(level = Some(0), loggingClass = Some("foo"), message = Some("a"))
      val pe1a = EntryFacets(e1a)
      val e1b = Entry(level = Some(0), loggingClass = Some("foo"), message = Some("b"))
      val pe1b = EntryFacets(e1b)
      val cc1 = mock[MockableConfiguration]("constrained")

      val k2 = EntryFacets(level = Some(Some(1)), loggingClass = Some(Some("foo")))
      val e2a = Entry(level = Some(1), loggingClass = Some("foo"), message = Some("a"))
      val pe2a = EntryFacets(e2a)
      val e2b = Entry(level = Some(1), loggingClass = Some("foo"), message = Some("b"))
      val pe2b = EntryFacets(e2b)
      val cc2 = mock[MockableConfiguration]("constrained")

      val k3 = EntryFacets(level = Some(Some(0)), loggingClass = Some(Some("bar")))
      val e3a = Entry(level = Some(0), loggingClass = Some("bar"), message = Some("a"))
      val pe3a = EntryFacets(e3a)
      val e3b = Entry(level = Some(0), loggingClass = Some("bar"), message = Some("b"))
      val pe3b = EntryFacets(e3b)
      val cc3 = mock[MockableConfiguration]("constrained")

      inSequence {
        (cfg.constrain _).expects(k1, false).returns(cc1).once
        (cc1.findReceivers _).expects(e1a).returns(Set.empty).once
        (cc1.findReceivers _).expects(e1b).returns(Set.empty).once
        (cfg.constrain _).expects(k2, false).returns(cc2).once
        (cc2.findReceivers _).expects(e2a).returns(Set.empty).once
        (cfg.constrain _).expects(k3, false).returns(cc3).once
        (cc3.findReceivers _).expects(e3a).returns(Set.empty).once
        (cc3.findReceivers _).expects(e3b).returns(Set.empty).once
        (cc2.findReceivers _).expects(e2b).returns(Set.empty).once
      }

      dispatcher.dispatch(e1a) // Trigger a call to constrain and findReceivers
      dispatcher.dispatch(e1b) // Use cached constrained config - trigger call to findReceivers only
      dispatcher.dispatch(e2a) // Trigger a call to constrain (for different level) and findReceivers
      dispatcher.dispatch(e3a) // Trigger a call to constrain (for different loggingClass) and findReceivers
      dispatcher.dispatch(e3b) // Use cached constrained config - trigger call to findReceivers only
      dispatcher.dispatch(e2b) // Use cached constrained config - trigger call to findReceivers only
    }

  }

  describe("CacheKeyExtractor factory") {

    trait KeyExtractorFixture {
      val callingClass = "org.scalawag.logger.A"
      val level = 14
      val threadName = Thread.currentThread.getName
      object TagB extends Tag
      val tags = Set[Tag](TagB)

      val entry = new Entry(
        loggingClass = Some(callingClass),
        level = Some(level),
        threadName = threadName,
        tags = tags,
        message = Some(Message.stringFnToMessage("msg"))
      )
    }

    it("should extract the right key - logger") {
      new KeyExtractorFixture {
        CacheKeyExtractor(Attribute.LoggingClass).extractKey(entry) shouldBe new EntryFacets(
          loggingClass = Some(Some(callingClass))
        )
      }
    }

    it("should extract the right key - level") {
      new KeyExtractorFixture {
        CacheKeyExtractor(Attribute.Level).extractKey(entry) shouldBe new EntryFacets(level = Some(Some(level)))
      }
    }

    it("should extract the right key - threadName") {
      new KeyExtractorFixture {
        CacheKeyExtractor(Attribute.ThreadName).extractKey(entry) shouldBe new EntryFacets(
          threadName = Some(threadName)
        )
      }
    }

    it("should extract the right key - tags") {
      new KeyExtractorFixture {
        CacheKeyExtractor(Attribute.Tags).extractKey(entry) shouldBe new EntryFacets(tags = Some(tags))
      }
    }

    it("should extract the right key - level & logger") {
      new KeyExtractorFixture {
        CacheKeyExtractor(Attribute.LoggingClass, Attribute.Level).extractKey(entry) shouldBe new EntryFacets(
          loggingClass = Some(Some(callingClass)),
          level = Some(Some(level))
        )
      }
    }

  }

}
