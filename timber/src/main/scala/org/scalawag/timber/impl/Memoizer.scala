package org.scalawag.timber.impl

object Memoizer {
  import Function._

  def memoize[T1,R](fn:(T1) => R):(T1) => R = new Memoizer(fn)
  def memoize[T1,T2,R](fn:(T1,T2) => R):(T1,T2) => R = untupled(new Memoizer(tupled(fn)))
  def memoize[T1,T2,T3,R](fn:(T1,T2,T3) => R):(T1,T2,T3) => R = untupled(new Memoizer(tupled(fn)))
  def memoize[T1,T2,T3,T4,R](fn:(T1,T2,T3,T4) => R):(T1,T2,T3,T4) => R = untupled(new Memoizer(tupled(fn)))
  def memoize[T1,T2,T3,T4,T5,R](fn:(T1,T2,T3,T4,T5) => R):(T1,T2,T3,T4,T5) => R = untupled(new Memoizer(tupled(fn)))
}

class Memoizer[T,R](fn:T => R) extends Function[T,R] {
  private var responses = Map[T,R]()

  def apply(args:T):R = responses.get(args) match {
    case Some(response) =>
      response
    case None =>
      val response = fn(args)
      responses += args -> response
      response
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
