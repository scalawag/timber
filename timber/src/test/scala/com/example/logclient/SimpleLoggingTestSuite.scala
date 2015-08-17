package com.example.logclient

import org.scalatest.{Matchers,FunSuite}
import org.scalatest.mock.MockitoSugar
import org.scalawag.timber.dsl._
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalawag.timber.impl.receiver.EntryReceiver
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher
import org.scalawag.timber.api.{Level, LoggerFactory, Logger}
import org.scalawag.timber.api.impl.Entry

class SimpleLoggingTestSuite extends FunSuite with Matchers with MockitoSugar {
  import Level.Implicits._

  test("test basic flow") {
    val r = mock[EntryReceiver]

    class MyLoggerManager extends SynchronousEntryDispatcher with LoggerFactory[Logger] {
      def getLogger(name: String): Logger = new Logger(name,this)
    }

    val lm = new MyLoggerManager {
      configure { IN =>
        IN :: ( logger.startsWith("com.example.logclient") ) :: ( level >= 2 ) :: r
        IN :: ( level >= 3 ) :: r
      }
    }

    val il = lm.getLogger("com.example.logclient.FakeClass")
    val el = lm.getLogger("org.apache.hadoop.Something")

    Iterable(il,el).foreach { l =>
      (0 to 4) foreach { level =>
        l.log(level,"level " + level)
      }
    }

    val captor = ArgumentCaptor.forClass(classOf[Entry])
    verify(r,times(5)).receive(captor.capture())

    val entries = captor.getAllValues

    entries.get(0).logger shouldBe il.name
    entries.get(0).level.level shouldBe 2

    entries.get(1).logger shouldBe il.name
    entries.get(1).level.level shouldBe 3

    entries.get(2).logger shouldBe il.name
    entries.get(2).level.level shouldBe 4

    entries.get(3).logger shouldBe el.name
    entries.get(3).level.level shouldBe 3

    entries.get(4).logger shouldBe el.name
    entries.get(4).level.level shouldBe 4
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
