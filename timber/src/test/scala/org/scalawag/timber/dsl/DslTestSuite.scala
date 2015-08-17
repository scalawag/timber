package org.scalawag.timber.dsl

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.api._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalawag.timber.impl.dispatcher.{SynchronousEntryDispatcher, Management}
import org.scalawag.timber.impl._
import dispatcher.Configuration
import formatter.DefaultEntryFormatter
import java.io.PrintWriter
import receiver.EntryReceiver
import org.scalawag.timber.api.impl.Entry
import org.scalawag.timber.api.style.slf4j

class DslTestSuite extends FunSuite with Matchers with MockitoSugar {
  import Level.Implicits._

  test("unnamed, open valve") {
    val e:Vertex = valve

    (e: @unchecked) match {
      case nv:NameableValve =>
        nv.open shouldBe true
    }
  }

  test("named, open valve") {
    val e:Vertex = valve.as("bob")

    (e: @unchecked) match {
      case nv:NamedValve =>
        nv.open shouldBe true
        nv.name shouldBe "bob"
    }
  }

  test("unnamed, closed valve") {
    val e:Vertex = valve.closed

    (e: @unchecked) match {
      case nv:NameableValve =>
        nv.open shouldBe false
    }
  }

  test("named, open valve (name before close)") {
    val e:Vertex = valve.as("bob").closed

    (e: @unchecked) match {
      case nv:NamedValve =>
        nv.open shouldBe false
        nv.name shouldBe "bob"
    }
  }

  test("named, open valve (close before name)") {
    val e:Vertex = valve.closed.as("bob")

    (e: @unchecked) match {
      case nv:NamedValve =>
        nv.open shouldBe false
        nv.name shouldBe "bob"
    }
  }

  test("lowest level filter") {
    val e:Vertex = ( level >= 5 )

    (e: @unchecked) match {
      case nf:NameableFilter =>
        (nf.condition: @unchecked) match {
          case llc:LowestLevelCondition =>
            llc.threshold shouldBe 5
        }
    }
  }

  test("named lowest level filter") {
    val e:Vertex = ( level >= 5 ).as("bob")

    (e: @unchecked) match {
      case nf:NamedFilter =>
        nf.name shouldBe "bob"
        (nf.condition: @unchecked) match {
          case llc:LowestLevelCondition =>
            llc.threshold shouldBe 5
        }
    }
  }

  test("highest level filter") {
    val e:Vertex = ( level <= 5 )

    (e: @unchecked) match {
      case nf:NameableFilter =>
        (nf.condition: @unchecked) match {
          case hlc:HighestLevelCondition =>
            hlc.threshold shouldBe 5
        }
    }
  }

  test("named highest level filter") {
    val e:Vertex = ( level <= 5 ).as("bob")

    (e: @unchecked) match {
      case nf:NamedFilter =>
        nf.name shouldBe "bob"
        (nf.condition: @unchecked) match {
          case hlc:HighestLevelCondition =>
            hlc.threshold shouldBe 5
        }
    }
  }

  test("logger starts with") {
    val e:Vertex = ( logger startsWith "bob" )

    (e: @unchecked) match {
      case f:Filter =>
        (f.condition: @unchecked) match {
          case lpc:LoggerPrefixCondition =>
            lpc.prefix shouldBe "bob"
        }
    }
  }

  test("context contains") {
    val e:Vertex = ( context("client") contains "bob" )

    (e: @unchecked) match {
      case f:Filter =>
        (f.condition: @unchecked) match {
          case ccc:ContextContainsCondition =>
            ccc.key shouldBe "client"
            ccc.value shouldBe "bob"
        }
    }
  }

  test("context is") {
    val e:Vertex = ( context("client") is "bob" )

    (e: @unchecked) match {
      case f:Filter =>
        (f.condition: @unchecked) match {
          case icec:InnermostContextEqualsCondition =>
            icec.key shouldBe "client"
            icec.value shouldBe "bob"
        }
    }
  }

  test("tagged") {
    object MyTag extends Tag
    val e:Vertex = ( tagged(MyTag) )

    (e: @unchecked) match {
      case f:Filter =>
        (f.condition: @unchecked) match {
          case tc:TaggedCondition =>
            tc.tag shouldBe MyTag
        }
    }
  }

  test("receiver wrapping") {
    val er = mock[EntryReceiver]

    val e:Vertex = er

    (e: @unchecked) match {
      case r:Receiver =>
        r.receiver shouldBe er
    }
  }

  test("multiple receiver wrapping") {
    val er = mock[EntryReceiver]

    val e1:Receiver = er
    val e2:Receiver = er

    // You should get different Receiver instances wrapping the same EntryReceiver.

    e1 should not be (e2)
    e1.receiver shouldBe e2.receiver
  }

  test("multiple condition wrapping") {
    val c = mock[Condition]

    val e1:Filter = c
    val e2:Filter = c

    // You should get different Filter instances wrapping the same Condition.

    e1 should not be (e2)
    e1.condition shouldBe e2.condition
  }

  test("direct circular paths should throw an exception at run-time") {
    val filter:Filter = ( level <= 5 )
    an [IllegalArgumentException] shouldBe thrownBy (filter :: filter)

    // Should leave in an unconnected state (without the cycle)
    filter.outputs shouldBe 'empty
  }

  test("indirect circular paths should throw an exception at run-time") {
    val f1:Filter = ( level <= 5 )
    val f2:Filter = ( level <= 6 )

    an [IllegalArgumentException] shouldBe thrownBy (f1 :: f2 :: f1)

    f1.outputs shouldBe 'empty
    f2.outputs shouldBe Set(f1)
  }

  ignore("mega test") {
    implicit val formatter = new DefaultEntryFormatter
    val IN = valve
    val so = stdout
    val se = stderr
    val pc = new HighestLevelCondition(8)

    val filter = ( level >= 8 )

    val mainValve = valve.as("Enable logging")
    val something = ( level >= 5 ).as("OtherLevel") :: so
    IN :: logger.startsWith("org.scalawag") :: ( level >= 3 ).as("MyLevel") :: so
    IN :: ( level >= 5 ).as("ThirdLevel") :: so
  //  val something = ( level >= 5 ) :: stdout
  //  IN :: logger.startsWith("org.scalawag") :: ( level >= 3 ) :: stdout
  //  IN :: ( level >= 5 ) :: stdout
    IN :: something
    IN :: ( level >= 9 ) :: mainValve
    IN :: ( level <= 14 ) :: mainValve
    IN :: filter :: mainValve
    IN :: filter :: pc.as("bob") :: mainValve
    mainValve :: valve.closed.as("Enable error log") :: file("ERRORS")
    mainValve :: so
    val appenders = (1 to 2) map { n =>
      val a = file("log." + n)
      IN :: ( level >= n ) :: a
      a
    }

    import ConfigTester.dump
    import Level.Implicits._

    val m1 = Entry("test","org.scalawag",0)
    val m2 = Entry("test","com.blah",2)
    val m3 = Entry("test","org.scalawag",1)

    var c = Configuration(ImmutableVertex(IN))

    dump(c,"blah.dot")

    c.findReceivers(m1) shouldBe Set[EntryReceiver](so)
    c.findReceivers(m2) shouldBe Set[EntryReceiver](so) ++ appenders
    c.findReceivers(m3) shouldBe Set[EntryReceiver](so) + appenders.head

    dump(c,"blah1.dot")

    val grok = file("GROK")
    IN :: grok
    val grok2 = file("GROK")
    IN :: grok2

    // should not take effect yet
    c.findReceivers(m1) shouldBe Set[EntryReceiver](so)

    c = ImmutableVertex(IN)

    dump(c,"blah2.dot")
    dump(c.constrain(PartialEntry(logger = Some("org.scalawag.timber.something"))),"blah3.dot")

    // now it should have taken effect
    c.findReceivers(m1) shouldBe Set[EntryReceiver](so,grok,grok2)

    val ed = new SynchronousEntryDispatcher
    ed.configuration = IN

    val lm = new slf4j.LoggerFactory(ed)

    val l = lm.getLogger("blah")
    l.error("This should print!!!")
//    grok.close
//    l.error("This should print again!!!")
//    grok.close
    val cause = new Throwable("cause")
    l.error(new Throwable("blah",cause))

    import Message._

    l.error { pw:PrintWriter =>
      pw.println("This is the message to print")
      new Throwable("blah my message").printStackTrace(pw)
    }

    def make(s:String):String = {
      System.err.println("making: " + s)
      s
    }

    l.log(0,"blah")
    l.log(0,"blah " + make("b") + 56)

    l.log(0) { o:PrintWriter =>
      o.println("justin")
      o.println("was")
      o.println("here")
    }
  }

  ignore("context filter") {
    val r1 = mock[EntryReceiver]
    val r2 = mock[EntryReceiver]

    object MyLoggerManager extends SynchronousEntryDispatcher {
      def getLogger(name:String):Logger = new Logger(name,this)

      configure { IN =>
        IN :: ( context("client") is "nobody" ) :: r1
        IN :: ( context("client") contains "nobody" ) :: r2
      }
    }

    val l = MyLoggerManager.getLogger("dummy")

    l.log(0,"this shouldn't print anywhere")

    verifyZeroInteractions(r1)
    verifyZeroInteractions(r2)

    LoggingContext.in(Map("client" -> "nobody")) {
      l.log(0,"this should print on both")

      verify(r1,times(1)).receive(any())
      verify(r2,times(1)).receive(any())

      LoggingContext.in(Map("client" -> "somebody")) {
        l.log(0,"this should print on contains only")

        verify(r1,times(1)).receive(any())
        verify(r2,times(2)).receive(any())
      }
    }
  }

  ignore("tag filters") {
    val r = mock[EntryReceiver]

    object AlertTag extends Tag

    object MyLoggerManager extends SynchronousEntryDispatcher {
      def getLogger(name:String):Logger = new Logger(name,this)

      configure { IN =>
        IN :: tagged(AlertTag) :: r
      }
    }

    val l = MyLoggerManager.getLogger("dummy")

    l.log(0,"this shouldn't reach the receiver")
    verifyZeroInteractions(r)

    l.log(0,"this should reach the receiver",AlertTag)
    verify(r,times(1)).receive(any())
  }

  ignore("not filters") {
    val r = mock[EntryReceiver]

    object AlertTag extends Tag

    object MyLoggerManager extends SynchronousEntryDispatcher {
      def getLogger(name:String):Logger = new Logger(name,this)

      configure { IN =>
        IN :: !tagged(AlertTag) :: r
      }
    }

    val l = MyLoggerManager.getLogger("dummy")

    l.log(0,"this shouldn't reach the receiver",AlertTag)
    verifyZeroInteractions(r)

    l.log(0,"this should reach the receiver")
    verify(r,times(1)).receive(any())
  }

  ignore("multiple paths, one receive") {
    val r = mock[EntryReceiver]

    object AlertTag extends Tag

    object MyLoggerManager extends SynchronousEntryDispatcher with LoggerFactory[Logger] {
      def getLogger(name:String):Logger = new Logger(name,this)

      configure { IN =>
        IN :: tagged(AlertTag) :: r
        IN :: ( level <= 5 ) :: r
        IN :: r
      }
    }

    val l = MyLoggerManager.getLogger("dummy")

    l.log(0,"this should reach the receiver exactly once",AlertTag)
    verify(r,times(1)).receive(any())
  }

  ignore("filter naming") {
    object MyLoggerManager extends SynchronousEntryDispatcher with LoggerFactory[Logger] with Management {
      def getLogger(name:String) = new Logger(name,this)

      configure { IN =>
        IN :: ( level >= 8 ).as("something")
      }
    }

    MyLoggerManager.getMBeanInfo.getAttributes.map(_.getName) shouldBe Array("something")
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
