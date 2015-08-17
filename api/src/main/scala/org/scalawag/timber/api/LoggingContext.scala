package org.scalawag.timber.api

object LoggingContext {
  private val contextThreadLocal = new ThreadLocal[Map[String,List[String]]] {
    override def initialValue() = Map[String,List[String]]()
  }

  def getInnermost = get map { case(k,v) => (k,v.head) }

  def get = contextThreadLocal.get

  def push(key:String,value:String):Unit = push(Map(key -> value))

  def push(entries:Map[String,String]) {
    val context = this.contextThreadLocal.get

    val (existingKeys,newKeys) = entries.partition( entry => context.contains(entry._1) )

    val newContext = context.map { case (key,existingValue) =>
      val v =
        existingKeys.get(key) match {
          case Some(newValue) =>
            newValue :: existingValue
          case None =>
            existingValue
        }
      key -> v
    } ++ newKeys.map { case (key,value) =>
      ( key -> List(value) )
    }.toMap

    this.contextThreadLocal.set(newContext)
  }

  def pop(key:String,value:String):Unit = pop(Map(key -> value))

  def pop(entries:Map[String,String]) {
    val context = this.contextThreadLocal.get

    // Check to make sure that all the removed keys are valid (that they're are the top of the stacks)

    entries.foreach { case (k,v) =>
      context.get(k) match {
        case Some(stack) =>
          if ( stack.head != v )
            throw new IllegalStateException("trying to pop (" + k + " -> " + v + ") from the logging context, but that key maps to '" + stack.head + "'")
        case None =>
          throw new IllegalStateException("trying to pop (" + k + " -> " + v + ") from the logging context, but that key isn't there")
      }
    }

    val newContext = context.flatMap { case (key,existingValue) =>
      entries.get(key) match {
        case Some(newValue) =>
          val stack = existingValue.tail
          if ( ! stack.isEmpty )
            Some( key -> stack )
          else
            None
        case None =>
          Some( key -> existingValue )
      }
    }.toMap

    this.contextThreadLocal.set(newContext)
  }

  def clear = this.contextThreadLocal.remove()

  def in[A](key:String,value:String)(fn: =>A):A = in(Map(key -> value))(fn)

  def in[A](entries:Map[String,String])(fn: =>A):A = {
    push(entries)
    try {
      fn
    } finally {
      pop(entries)
    }
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
