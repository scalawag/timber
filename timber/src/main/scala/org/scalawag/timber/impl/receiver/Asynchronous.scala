package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.{NamedThreadFactory, LoggerImpl, InternalLogging, Entry}
import java.util.concurrent._
import org.scalawag.timber.api.{Logger, LoggerFactory}
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher
import java.util.concurrent.TimeUnit

class Asynchronous(private val receiver:EntryReceiver) extends EntryReceiver { self =>
  private val executor = new ThreadPoolExecutor(0,1,500L,TimeUnit.MILLISECONDS,
                                                new LinkedBlockingQueue[Runnable],
                                                new NamedThreadFactory(receiver.toString))

  override def receive(entry:Entry) = {
    executor.submit(new Runnable {
      def run() {
        self.receiver.receive(entry)
      }
    })
  }

  override lazy val toString = "Async(%s)".format(receiver)
}

object Asynchronous {
  def apply(receiver:EntryReceiver) = new Asynchronous(receiver)

  def testCreate {
    val r = new EntryReceiver {
      def receive(entry: Entry) {
        Thread.sleep(1000)
        println("RR:" + entry)
      }

      override def toString = "TestReceiver"
    }
    val a = new Asynchronous(r)

    val lm = new SynchronousEntryDispatcher[Logger] with LoggerFactory[Logger] {
      override def getLogger(name:String):Logger = new LoggerImpl(name,this)

      configure { IN =>
        import org.scalawag.timber.dsl._
        IN :: a
      }
    }

    val l = lm.getLogger("category")

    (1 to 10).foreach { n =>
      l.log(8,"msg " + n)
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
