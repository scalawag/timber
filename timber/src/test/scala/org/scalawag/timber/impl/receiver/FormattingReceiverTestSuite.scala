package org.scalawag.timber.impl.receiver

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import java.io.Writer
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalawag.timber.impl.formatter.EntryFormatter
import org.mockito.ArgumentCaptor
import org.scalawag.timber.api.Level
import org.scalawag.timber.api.impl.Entry

class FormattingReceiverTestSuite extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  private val entry = new Entry("foo","logger",Level(0))

  private val w = mock[Writer]
  private val f = mock[EntryFormatter]
  when(f.format(any[Entry])).thenReturn("bar")

  private val r = new FormattingReceiver(f) {
    override val writer = w
  }

  test("receive and format an entry") {
    r.receive(entry)

    val captor = ArgumentCaptor.forClass(classOf[Entry])
    verify(f,times(1)).format(captor.capture)
    captor.getValue.message.text should be ("foo")
    verifyNoMoreInteractions(f)

    verify(w,times(1)).write("bar")
    verifyNoMoreInteractions(w)
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
