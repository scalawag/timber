package com.example.logclient

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.impl.{Locationable, Location}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalawag.timber.impl.dispatcher.EntryDispatcher
import org.mockito.ArgumentCaptor
import org.scalawag.timber.impl.Locationable.WithLocation
import scala.collection.JavaConversions._
import org.scalawag.timber.api._
import org.scalawag.timber.api.impl.Entry

class LocationTestSuite extends FunSuite with ShouldMatchers with MockitoSugar {
  import Level.Implicits._

  test("location determination") {
    val dispatcher = mock[EntryDispatcher]
    val l = new Logger("name",dispatcher) with Location

    l.log(0,"blah") // This is the line above (see below).

    val captor = ArgumentCaptor.forClass(classOf[Entry])
    verify(dispatcher,times(1)).dispatch(captor.capture())

    // This is extremely sensitive to file modifications.  Update it to match the number of the line above if it fails.
    captor.getValue.location should be (Some(Entry.Location("LocationTestSuite.scala",22)))
  }

  test("no location determination (no mix-in)") {
    val dispatcher = mock[EntryDispatcher]
    val l = new Logger("name",dispatcher)

    l.log(0,"blah")

    val captor = ArgumentCaptor.forClass(classOf[Entry])
    verify(dispatcher,times(1)).dispatch(captor.capture())
    captor.getValue.location should be (None)
  }

  test("locationable (optional location determination)") {
    val dispatcher = mock[EntryDispatcher]
    val l = new Logger("name",dispatcher) with Locationable

    l.log(0,"blah")
    l.log(0,"blah",WithLocation) // This is the line above (see below).

    val captor = ArgumentCaptor.forClass(classOf[Entry])
    verify(dispatcher,times(2)).dispatch(captor.capture())

    val entries = captor.getAllValues.map(_.location)

    entries.get(0) should be (None)
    // This is extremely sensitive to file modifications.  Update it to match the number of the line above if it fails.
    entries.get(1) should be (Some(Entry.Location("LocationTestSuite.scala",47)))
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
