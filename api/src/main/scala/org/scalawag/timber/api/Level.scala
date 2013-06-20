package org.scalawag.timber.api

// need an example of how to add a new level and then use it in the configuration

object Level {
  val TRACE = Level(1000,"TRACE")
  val DEBUG = Level(2000,"DEBUG")
  val INFO  = Level(3000,"INFO")
  val WARN  = Level(4000,"WARN")
  val ERROR = Level(5000,"ERROR")

  def apply(severity:Int,name:String):Level = apply(severity,Some(name))

  object Implicits {
    implicit def intToLevel(level:Int) = apply(level)
    implicit def levelToInt(level:Level) = level.level
  }
}

case class Level(level:Int,name:Option[String] = None) {
  def as(alias:String) = copy(name = Some(alias))
  def +(increment:Int) = copy(level = level + increment)
  def -(decrement:Int) = copy(level = level - decrement)
  override lazy val toString = name.getOrElse(level.toString)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
