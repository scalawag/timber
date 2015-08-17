package org.scalawag.timber.impl.receiver

import org.scalatest.{OneInstancePerTest,FunSuite}
import org.scalatest.mock.MockitoSugar
import java.io.PrintWriter
import org.mockito.Mockito._
import org.scalawag.timber.impl.formatter.DefaultEntryFormatter
import org.scalawag.timber.api.Level
import org.scalawag.timber.api.impl.Entry

class AutoFlushTestSuite extends FunSuite with MockitoSugar with OneInstancePerTest {
  private val pw = mock[PrintWriter]
  private val oneLineEntry = new Entry("foo","logger",Level(0))
  private val twoLineEntry = new Entry("foo\nbar","logger",Level(0))

  test("receive without AutoFlush") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter)
    receiver.receive(oneLineEntry)
    verify(pw,times(0)).flush()
  }

  test("receive with AutoFlush") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter) with AutoFlush
    receiver.receive(oneLineEntry)
    verify(pw,times(1)).flush()
  }

  test("receive with AutoFlush (and multi-line message)") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter) with AutoFlush
    receiver.receive(twoLineEntry)
    verify(pw,times(1)).flush()
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
