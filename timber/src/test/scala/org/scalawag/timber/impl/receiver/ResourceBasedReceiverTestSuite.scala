package org.scalawag.timber.impl.receiver

import org.scalatest.{Matchers,OneInstancePerTest,FunSuite}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import java.io.Writer
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.scalawag.timber.impl.formatter.EntryFormatter
import org.scalawag.timber.api.Level
import org.scalawag.timber.api.impl.Entry

class ResourceBasedReceiverTestSuite extends FunSuite with Matchers with MockitoSugar with OneInstancePerTest{
  private val w = mock[Writer]
  private val entry = new Entry("foo","logger",Level(0))

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
    receiver.writer shouldBe w
    verify(receiver,times(1)).open
  }

  test("calling writer twice calls open once") {
    receiver.writer shouldBe w
    receiver.writer shouldBe w
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
    an [IllegalStateException] shouldBe thrownBy (receiver.writer)
  }

  test("calling close before writer is OK") {
    receiver.close
  }

  test("calling writer after close fails even if the resource hadn't been open") {
    receiver.close
    an [IllegalStateException] shouldBe thrownBy (receiver.writer)
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
