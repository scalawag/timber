package org.scalawag.timber.api

// need an example of how to add a new level and then use it in the configuration

object Level {
  val FINEST  = Level(1000,"FINEST")
  val FINER   = Level(2000,"FINER")
  val FINE    = Level(3000,"FINE")
  val DEBUG   = Level(4000,"DEBUG")
  val INFO    = Level(5000,"INFO")
  val WARNING = Level(6000,"WARNING")
  val ERROR   = Level(7000,"ERROR")
  val FATAL   = Level(8000,"FATAL")

  def apply(severity:Int,name:String):Level = apply(severity,Some(name))

  object Implicits {
    implicit def intToLevel(level:Int) = apply(level)
    implicit def levelToInt(level:Level) = level.level
  }
}

case class Level(level:Int,name:Option[String] = None) {
  def as(alias:String) = copy(name = Some(alias))
  def +(increment:Int) = copy(level = level + increment)
  override lazy val toString = name.getOrElse(level.toString)
}
