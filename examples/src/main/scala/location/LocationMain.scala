package location

import org.scalawag.timber.api.style.slf4j._
import org.scalawag.timber.impl.{Locationable, Location}
import org.scalawag.timber.impl.Locationable.WithLocation
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher

object LocationMain {
  def main(args:Array[String]) {
    val dispatcher = new SynchronousEntryDispatcher
    val loggers = Seq(
      new Logger("Locationable",dispatcher) with Locationable,
      new Logger("Location",dispatcher) with Location
    )

    loggers foreach { l =>
      l.debug("blah")
      l.debug("blah",WithLocation)
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
