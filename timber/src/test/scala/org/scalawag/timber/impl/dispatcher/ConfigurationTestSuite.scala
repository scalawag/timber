package org.scalawag.timber.impl.dispatcher

import org.scalatest.{Matchers,FunSuite}
import org.scalatest.mock.MockitoSugar
import org.scalawag.timber.impl.receiver.EntryReceiver
import org.scalawag.timber.impl.{ImmutableFilter, PartialEntry, ImmutableReceiver, ImmutableValve}
import org.scalawag.timber.dsl.Condition
import org.mockito.Mockito._
import org.mockito.Matchers._

class ConfigurationTestSuite extends FunSuite with Matchers with MockitoSugar {
  private val trueCondition = mock[Condition]
  when(trueCondition.allows(any[PartialEntry])).thenReturn(Some(true))
  private val falseCondition = mock[Condition]
  when(falseCondition.allows(any[PartialEntry])).thenReturn(Some(false))
  private val noneCondition = mock[Condition]
  when(noneCondition.allows(any[PartialEntry])).thenReturn(None)

  test("constrain removes closed valves (and everything below)") {
    val r = mock[EntryReceiver]
    val cfg = Configuration(ImmutableValve(false,Set(ImmutableReceiver(r))))

    cfg.constrain() shouldBe Configuration()
  }

  test("constrain removes closed valves (and everything below (unless otherwise reachable))") {
    val r3 = new ImmutableReceiver(mock[EntryReceiver])
    val f2 = new ImmutableFilter(noneCondition,Set(r3))
    val v2 = new ImmutableValve(false,Set(r3))
    val f1 = new ImmutableFilter(noneCondition,Set(f2,v2))

    val f1a = f1.copy(outs = Set(f2))

    Configuration(f1).constrain() shouldBe Configuration(f1a)
  }

  test("constrain collapses open valves") {
    val r3 = new ImmutableReceiver(mock[EntryReceiver])
    val f2 = new ImmutableFilter(noneCondition,Set(r3))
    val v1 = new ImmutableValve(true,Set(f2))

    Configuration(v1).constrain() shouldBe Configuration(f2)
  }

  test("constrain removes filters that block the entry (and everything below)") {
    val r2 = new ImmutableReceiver(mock[EntryReceiver])
    val f1 = new ImmutableFilter(falseCondition,Set(r2))

    Configuration(f1).constrain() shouldBe Configuration()
  }

  test("constrain removes filters that block the entry (and everything below (unless otherwise reachable))") {
    val r3 = new ImmutableReceiver(mock[EntryReceiver])
    val f2a = new ImmutableFilter(noneCondition,Set(r3))
    val f2b = new ImmutableFilter(falseCondition,Set(r3))
    val f1 = new ImmutableFilter(noneCondition,Set(f2a,f2b))

    val expected = f1.copy(outs = Set(f2a))

    Configuration(f1).constrain() shouldBe Configuration(expected)
  }

  test("constrain maintains filters that abstain") {
    val r2 = new ImmutableReceiver(mock[EntryReceiver])
    val f1 = new ImmutableFilter(noneCondition,Set(r2))

    Configuration(f1).constrain() shouldBe Configuration(f1)
  }

  test("constrain collapses filters that allow the entry") {
    val r2 = new ImmutableReceiver(mock[EntryReceiver])
    val f1 = new ImmutableFilter(trueCondition,Set(r2))

    Configuration(f1).constrain() shouldBe Configuration(r2)
  }

  test("constrain removes everything leading up to a dead end (no receiver)") {
    val cfg = Configuration(ImmutableFilter(noneCondition,Set(ImmutableFilter(noneCondition,Set(ImmutableFilter(noneCondition,Set()))))))

    cfg.constrain() shouldBe Configuration()
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
