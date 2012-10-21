package org.scalawag.timber.api

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

// This test is just here to ensure that this use case continues to work.
// The use case is sharing a logger between many instances using a companion object.

private object Companion extends slf4j.Logging {
  override protected lazy val log = loggerFactory.getLogger[Companion]
}

private class Companion {
  import Companion.log
  def bark = log.debug("ARF")
}

class LoggerFactoryTestSuite extends FunSuite with ShouldMatchers {

  test("logger should have undecorated name") {
//    Companion.log.name should be ("org.scalawag.timber.api.Companion")
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
