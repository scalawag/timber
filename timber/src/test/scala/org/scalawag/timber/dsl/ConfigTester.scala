package org.scalawag.timber.dsl

import debug.DotDumper
import java.io.{FileWriter, PrintWriter}
import org.scalawag.timber.impl._
import dispatcher.{Configuration, EntryDispatcher}
import formatter.DefaultEntryFormatter
import org.scalawag.timber.api._
import receiver.{Asynchronous, EntryReceiver, StderrReceiver, StdoutReceiver}
import util.Random

object ConfigTester {
  implicit val formatter = new DefaultEntryFormatter
  val IN = valve
  val stdout = Asynchronous(new StdoutReceiver(formatter))
  val stderr = Asynchronous(new StderrReceiver(formatter))

  val filter = ( level >= 8 )

  val mainValve = valve.as("Enable logging")
  val something = ( level >= 5 ).as("Minimum Level (non-scalawag code)") :: stdout
  IN :: logger.startsWith("org.scalawag") :: ( level >= 3 ).as("Minimum Level (scalawag code)") :: stdout
  IN :: ( level >= 5 ) :: stdout
//  val something = ( level >= 5 ) :: stdout
//  IN :: logger.startsWith("org.scalawag") :: ( level >= 3 ) :: stdout
//  IN :: ( level >= 5 ) :: stdout
  IN :: something
  IN :: ( level <= 14 ) :: mainValve
  IN :: filter :: mainValve
  IN :: filter :: mainValve
  mainValve :: valve.closed.as("Enable error log") :: file("ERRORS")
  mainValve :: stdout
  (1 to 2) foreach { n =>
    IN :: ( level >= n ) :: file("log." + n)
  }

  slf4j.LoggerManager.configuration = IN

  def dump(vertex:ImmutableVertex) {
    val p = new PrintWriter(System.out)
    DotDumper.dump(vertex,p)
    p.flush
  }

  def dump(configuration:Configuration,filename:String) {
    val p = new PrintWriter(new FileWriter(filename))
    DotDumper.dump(configuration,p)
    p.close
  }

  def dumping {
    dump(slf4j.LoggerManager.configuration,"blah.dot")
    val c = slf4j.LoggerManager.configuration.constrain()
    dump(c,"blah2.dot")
  }

  def reg(o:AnyRef) {
    import management.ManagementFactory
    import javax.management.ObjectName
    val mbeanServer = ManagementFactory.getPlatformMBeanServer
    mbeanServer.registerMBean(o,new ObjectName("org.scalawag.timber:type=LoggerManager,name=Global"))
  }

  import org.scalawag.timber.impl._
  import org.scalawag.timber.api._

  object Dispatcher extends EntryDispatcher {
    def dispatch(event: Entry) {
      // noop
    }
  }

  val l = new LoggerImpl("dummy",Dispatcher) with slf4j.Trace with slf4j.Debug with slf4j.Error with slf4j.Warn with slf4j.Fatal with slf4j.Info

  def speed(l:Logger) = {
    val random = new Random
    val start = System.currentTimeMillis
    (1 to 100000) foreach { n =>
      val level = ( random.nextInt(5) + 1 ) * 10
      l.log(level,"testing")
    }
    val time = System.currentTimeMillis - start
    println(time + " ms")
    time
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
