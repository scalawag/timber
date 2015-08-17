package org.scalawag.timber.api

import org.scalatest.{Matchers,FunSuite}
import java.io.PrintWriter

class MessageTestSuite extends FunSuite with Matchers {

  test("gatherer conversion") {
    val text = "blah"
    val msg:Message = { pw:PrintWriter =>
      pw.print(text)
    }

    msg.text shouldBe text
  }

  test("throwable conversion") {
    val text = "blah"
    val msg:Message = new Throwable(text)

    msg.text.contains(text) shouldBe true
    msg.text.contains(this.getClass.getName) shouldBe true
  }

  test("getLines") {
    val lines = Traversable("foo","bar")
    val msg:Message = lines.mkString("","\n","\n")

    msg.lines shouldBe lines
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
