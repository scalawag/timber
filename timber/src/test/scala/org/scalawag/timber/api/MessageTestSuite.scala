package org.scalawag.timber.api

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import Message._
import java.io.PrintWriter

class MessageTestSuite extends FunSuite with ShouldMatchers {

  test("gatherer conversion") {
    val text = "blah"
    val msg:Message = { pw:PrintWriter =>
      pw.print(text)
    }

    msg.text should be (text)
  }

  test("throwable conversion") {
    val text = "blah"
    val msg:Message = new Throwable(text)

    msg.text.contains(text) should be (true)
    msg.text.contains(this.getClass.getName) should be (true)
  }

  test("getLines") {
    val lines = Traversable("foo","bar")
    val msg:Message = lines.mkString("","\n","\n")

    msg.lines should be (lines)
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
