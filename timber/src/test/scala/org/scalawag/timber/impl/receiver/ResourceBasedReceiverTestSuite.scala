package org.scalawag.timber.impl.receiver

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import java.io.Writer
import org.scalawag.timber.impl.Entry
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.scalawag.timber.impl.formatter.EntryFormatter

class ResourceBasedReceiverTestSuite extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest{
  private val w = mock[Writer]
  private val entry = new Entry("foo","logger",0,"DEBUG")

  // A formatter that just regurgitates the message lines as-is.
  private val formatter = mock[EntryFormatter]
  when(formatter.format(any[Entry])).thenAnswer(new Answer[String] {
    def answer(invocation: InvocationOnMock): String =
      invocation.getArguments.apply(0).asInstanceOf[Entry].message.text
  })

  class TestReceiver extends ResourceBasedReceiver {
    override def open = w
    override def receive(entry: Entry) {} // just needed for concreteness
    override def writer = super.writer // making it accessible to the tests
  }

  private val receiver = spy(new TestReceiver)

  test("calling writer calls open") {
    receiver.writer should be (w)
    verify(receiver,times(1)).open
  }

  test("calling writer twice calls open once") {
    receiver.writer should be (w)
    receiver.writer should be (w)
    verify(receiver,times(1)).open
  }

  test("calling close calls underlying close") {
    receiver.writer
    receiver.close
    verify(w,times(1)).close
  }

  test("calling close twice calls underlying close once") {
    receiver.writer
    receiver.close
    receiver.close
    verify(w,times(1)).close
  }

  test("calling writer after close fails") {
    receiver.writer
    receiver.close
    evaluating {
      receiver.writer
   } should produce [IllegalStateException]
  }

  test("calling close before writer is OK") {
    receiver.close
  }

  test("calling writer after close fails even if the resource hadn't been open") {
    receiver.close
    evaluating {
      receiver.writer
   } should produce [IllegalStateException]
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
