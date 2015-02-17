package org.scalawag.timber.impl

import org.scalatest.{Matchers,FunSuite}
import org.scalawag.timber.dsl._
import org.scalatest.mock.MockitoSugar
import receiver.EntryReceiver

class ImmutableVertexTestSuite extends FunSuite with Matchers with MockitoSugar {

  test("apply - convert Filter") {
    val c = mock[Condition]
    val f = new Filter(c)

    val ie = ImmutableVertex(f)

    narrow(ie) { f:ImmutableFilter =>
      f.condition shouldBe c
      f.name shouldBe None
    }
  }

  test("apply - convert NamedFilter") {
    val c = mock[Condition]
    val f = new NamedFilter("bob",c)

    val ie = ImmutableVertex(f)

    narrow(ie) {f:ImmutableFilter =>
      f.condition shouldBe c
      f.name shouldBe Some("bob")
    }
  }

  def valveTest(open:Boolean) {
    val v = new Valve(open)
    val ie = ImmutableVertex(v)

    narrow(ie) { iv:ImmutableValve =>
      iv.open shouldBe open
      iv.name shouldBe None
    }
  }

  test("apply - convert open Valve")(valveTest(true))

  test("apply - convert closed Valve")(valveTest(false))

  def namedValveTest(open:Boolean) {
    val v = new NamedValve(open,"bob")
    val ie = ImmutableVertex(v)

    narrow(ie) { iv:ImmutableValve =>
      iv.open shouldBe open
      iv.name shouldBe Some("bob")
    }
  }

  test("apply - convert open NamedValve")(namedValveTest(true))

  test("apply - convert closed NamedValve")(namedValveTest(false))

  test("apply - convert Receiver") {
    val er = mock[EntryReceiver]
    val r = new Receiver(er)

    val ie = ImmutableVertex(r)

    narrow(ie) { ir:ImmutableReceiver =>
      ir.receiver shouldBe er
    }
  }

  test("apply - deduplicate common nodes") {
    val v1 = valve.as("v1")
    val v2 = valve.as("v2")
    val v3 = valve.as("v3")
    val v4 = valve.as("v4")

    v1 :: v2 :: v4
    v1 :: v3 :: v4

    val ie = ImmutableVertex(v1)

    narrow(ie) { iv:ImmutableValve =>
      iv.outs.size shouldBe 2

      // Both filters should point to the same ImmutableValve
      val outsouts = iv.outs flatMap { o =>
        narrow(o) { iv:ImmutableValve => iv.outs }
      }

      outsouts.size shouldBe 1
      outsouts.head.name shouldBe Some("v4")
    }
  }

  test("apply - deduplicate common receivers") {
    val er = mock[EntryReceiver]

    // The DSL wraps the EntryReceiver in a new Receiver every time it's attached to another vertex.
    // In the immutable world, those should be deduplicated.

    val v = valve
    v :: er
    v :: er

    val ie = ImmutableVertex(v)

    narrow(ie) { iv:ImmutableValve =>
      iv.outs.size shouldBe 1
      narrow(iv.outs.head) { ir:ImmutableReceiver =>
        ir.receiver shouldBe er
      }
    }
  }

  test("apply - do not deduplicate common conditions") {
    val c = mock[Condition]

    // The DSL should create a new Filter for each Condition every time it's attached to another vertex.  In the
    // immutable world, those should be remain separate ImmutableFilters.

    val v = valve
    v :: c :: mock[EntryReceiver]
    v :: c :: mock[EntryReceiver]

    val ie = ImmutableVertex(v)

    narrow(ie) { iv:ImmutableValve =>
      iv.outs.size shouldBe 2
    }
  }

  test("map") {
    val v4 = ImmutableValve(true,Set(),Some("v4"))
    val v3 = ImmutableValve(true,Set(v4),Some("v3"))
    val v2 = ImmutableValve(true,Set(v4),Some("v2"))
    val v1 = ImmutableValve(true,Set(v2,v3),Some("v1"))

    var count = 0
    val mapped = v1.map { (src,outs) =>
      narrow(src) { iv:ImmutableValve =>
        count += 1
        new ImmutableValve(false,outs,iv.name.map(_ + "m"))
      }
    }

    count shouldBe 4

    val v4m = ImmutableValve(false,Set(),Some("v4m"))
    val v3m = ImmutableValve(false,Set(v4m),Some("v3m"))
    val v2m = ImmutableValve(false,Set(v4m),Some("v2m"))
    val v1m = ImmutableValve(false,Set(v2m,v3m),Some("v1m"))

    mapped shouldBe v1m
  }

  test("flatMap - no pruning") {
    val v5 = ImmutableValve(true,Set(),Some("v5"))
    val v4 = ImmutableValve(true,Set(),Some("v4"))
    val v3 = ImmutableValve(true,Set(v4),Some("v3"))
    val v2 = ImmutableValve(true,Set(v4),Some("v2"))
    val v1 = ImmutableValve(true,Set(v2,v3),Some("v1"))

    var count = 0

    def prune(e:ImmutableVertex) = false

    def transform(src:ImmutableVertex,outs:Set[ImmutableVertex]):Set[ImmutableVertex] = {
      narrow(src) { iv:ImmutableValve =>
        count += 1
        iv.name match {
          case Some("v2") => Set()
          case Some("v4") => Set(v4,v5)
          case _ => Set(new ImmutableValve(iv.open,outs,iv.name))
        }
      }
    }

    val mapped = v1.flatMap(prune,transform)

    count shouldBe 4

    val v3m = ImmutableValve(true,Set(v4,v5),Some("v3"))
    val v1m = ImmutableValve(true,Set(v3m),Some("v1"))

    mapped shouldBe v1m
  }

  test("flatMap - with pruning") {
    val v4 = ImmutableValve(true,Set(),Some("v4"))
    val v3 = ImmutableValve(true,Set(v4),Some("v3"))
    val v2 = ImmutableValve(true,Set(v4),Some("v2"))
    val v1 = ImmutableValve(true,Set(v2,v3),Some("v1"))

    var count = 0

    def prune(e:ImmutableVertex) = narrow(e) { iv:ImmutableValve =>
      iv.name.get == "v3"
    }

    def transform(src:ImmutableVertex,outs:Set[ImmutableVertex]):Set[ImmutableVertex] = {
      narrow(src) { iv:ImmutableValve =>
        count += 1
        Set(new ImmutableValve(iv.open,outs,iv.name))
      }
    }

    val mapped = v1.flatMap(prune,transform)

    count shouldBe 3 // 1 was pruned

    val v1m = ImmutableValve(true,Set(v2),Some("v1"))

    mapped shouldBe v1m
  }

  test("reconfigure - Boolean") {
    val v4 = ImmutableValve(true,Set(),Some("v4"))
    val v3 = ImmutableValve(true,Set(v4),Some("v3"))
    val v2 = ImmutableValve(true,Set(v4),Some("v2"))
    val v1 = ImmutableValve(true,Set(v2,v3),Some("v1"))

    val reconfigured = v1.reconfigure("v2",false:java.lang.Boolean)

    val v2m = v2.copy(open = false)
    val v1m = v1.copy(outs = Set(v2m,v3))

    reconfigured shouldBe v1m
  }

  test("reconfigure - Int") {
    val v4 = ImmutableFilter(new LowestLevelCondition(4),Set(),Some("v4"))
    val v3 = ImmutableFilter(new LowestLevelCondition(3),Set(v4),Some("v3"))
    val v2 = ImmutableFilter(new LowestLevelCondition(2),Set(v4),Some("v2"))
    val v1 = ImmutableFilter(new LowestLevelCondition(1),Set(v2,v3),Some("v1"))

    val reconfigured = v1.reconfigure("v2",8:java.lang.Integer)

    val v2m = v2.copy(new LowestLevelCondition(8))
    val v1m = v1.copy(outs = Set(v2m,v3))

    reconfigured shouldBe v1m
  }

  private def narrow[T:Manifest,A](obj:Any)(fn:T => A) = {
    if ( manifest.runtimeClass.isAssignableFrom(obj.getClass) ) {
      fn(obj.asInstanceOf[T])
    } else {
      fail("object " + obj + " is not of the expected type (" + manifest.runtimeClass + ")")
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
