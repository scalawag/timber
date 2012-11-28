package org.scalawag.timber.impl.dispatcher

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.impl.Entry
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalawag.timber.api.{Message, Tag, Logger}
import org.scalawag.timber.impl.{PartialEntry, LoggerImpl}
import org.scalawag.timber.impl.receiver.EntryReceiver

class ConfigurationCacheTestSuite extends FunSuite with ShouldMatchers with MockitoSugar {

  test("without cache, each getReceivers call should create a new config") {
    val cfg = mock[Configuration]
    when(cfg.findReceivers(any())).thenReturn(Set[EntryReceiver]())

    val lm = new SynchronousEntryDispatcher[Logger](cfg) {
      def getLogger(name: String): Logger = new LoggerImpl(name,this)
    }

    val l1 = lm.getLogger("foo")

    verify(cfg,never).findReceivers(any())

    l1.log(0,"blah") // Should trigger a call to constrain

    verify(cfg,times(1)).findReceivers(any())

    l1.log(0,"blah") // Should trigger another call to constrain

    verify(cfg,times(2)).findReceivers(any())
  }

  test("with cache, each getReceivers call should reuse the same config") {
    val cfg = mock[Configuration]
    val constrained = mock[Configuration]
    when(cfg.constrain(any[PartialEntry],any[Boolean])).thenReturn(constrained)
    when(constrained.findReceivers(any())).thenReturn(Set[EntryReceiver]())

    val lm = new SynchronousEntryDispatcher[Logger](cfg) with ConfigurationCache {
      def getLogger(name: String): Logger = new LoggerImpl(name,this)

      def extractKey(entry: Entry): PartialEntry = PartialEntry(logger = Some(entry.logger),
                                                                level = Some(entry.level))
    }

    val l1 = lm.getLogger("foo")
    val l2 = lm.getLogger("bar")

    verify(cfg,never).constrain(any())

    l1.log(0,"blah") // Should trigger a call to constrain

    verify(cfg,times(1)).constrain(any[PartialEntry],any[Boolean])

    l1.log(0,"blah") // The constrained configuration should be cached

    verify(cfg,times(1)).constrain(any[PartialEntry],any[Boolean])

    l1.log(1,"blah") // Call to a different level should trigger another constrain call

    verify(cfg,times(2)).constrain(any[PartialEntry],any[Boolean])

    l2.log(0,"blah") // Call to a different logger should trigger another constrain call

    verify(cfg,times(3)).constrain(any[PartialEntry],any[Boolean])
  }

  trait KeyExtractorFixture {
    val logger = "loggerA"
    val level = 14
    val thread = Thread.currentThread
    object TagB extends Tag
    val tags = Set[Tag](TagB)

    val entry = new Entry(logger = logger,
                          level = level,
                          thread = thread,
                          tags = tags,
                          message = Message.stringFnToMessage("msg"),
                          levelName = level.toString)
  }

  import ConfigurationCache._

  test("keyExtractor extracts the right key - logger") {
    new KeyExtractorFixture {
      ConfigurationCache.keyExtractor(entry,Attribute.Logger) should be (new PartialEntry(logger = Some(logger)))
    }
  }

  test("keyExtractor extracts the right key - level") {
    new KeyExtractorFixture {
      ConfigurationCache.keyExtractor(entry,Attribute.Level) should be (new PartialEntry(level = Some(level)))
    }
  }

  test("keyExtractor extracts the right key - thread") {
    new KeyExtractorFixture {
      ConfigurationCache.keyExtractor(entry,Attribute.Thread) should be (new PartialEntry(thread = Some(thread)))
    }
  }

  test("keyExtractor extracts the right key - tags") {
    new KeyExtractorFixture {
      ConfigurationCache.keyExtractor(entry,Attribute.Tags) should be (new PartialEntry(tags = Some(tags)))
    }
  }

  test("keyExtractor extracts the right key - level & logger") {
    new KeyExtractorFixture {
      val fn = ConfigurationCache.keyExtractor(_:Entry,Attribute.Logger,Attribute.Level)
      fn(entry) should be (new PartialEntry(logger = Some(logger),level = Some(level)))
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
