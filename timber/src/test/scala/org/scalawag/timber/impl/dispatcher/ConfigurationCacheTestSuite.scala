package org.scalawag.timber.impl.dispatcher

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.impl.Entry
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalawag.timber.api.Logger
import org.scalawag.timber.impl.{PartialEntry, LoggerImpl, ImmutableVertex}
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

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
