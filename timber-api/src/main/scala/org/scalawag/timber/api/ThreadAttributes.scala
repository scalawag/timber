// timber -- Copyright 2012-2021 -- Justin Patterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalawag.timber.api

/** Provides a convenient mechanism for associating thread-specific attributes with your log [[Entry entries]].
  * [[BaseLogger Loggers]] will copy the thread-specific attributes available through this object
  * (ThreadAttributes.get()) into the `threadAttributes` field of every entry they create.
  *
  * Thread attribute values are stacked, making it possible to push an attribute value for a specific code block and
  * then pop the value from the attribute stack, leaving the thread attributes in the state that was present before
  * the block was executed. This is directly implemented in the `during()` method calls.  It can also be managed
  * explicitly by the calling code (e.g., when the push and pop operations need to span method bodies) using the
  * `push` and `pop` methods.
  */
object ThreadAttributes {
  private val contextThreadLocal = new ThreadLocal[Map[String, List[String]]] {
    override def initialValue(): Map[String, List[String]] = Map[String, List[String]]()
  }

  /** Retrieves the current thread attributes.
    *
    * @return the topmost value on the stack for each attribute on this thread as a map
    */
  def getTopmost: Map[String, String] = get map { case (k, v) => (k, v.head) }

  /** Retrieves the current thread attributes.
    *
    * @return the stack of values for each attribute on this thread as a map of lists
    */
  def get: Map[String, List[String]] = contextThreadLocal.get

  /** Pushes a new value onto the stack for the named thread attribute.
    *
    * @param name the name of the attribute for which to push a value
    * @param value the value to push for the named attribute
    */
  def push(name: String, value: String): Unit = push(Map(name -> value))

  /** Pushes a collection of values onto the stacks of several thread attributes.
    *
    * @param attributes the thread attribute names and values to push
    */
  def push(attributes: Map[String, String]): Unit = {
    val context = this.contextThreadLocal.get

    val (existingKeys, newKeys) = attributes.partition(entry => context.contains(entry._1))

    val newContext = context.map {
      case (key, existingValue) =>
        val v =
          existingKeys.get(key) match {
            case Some(newValue) =>
              newValue :: existingValue
            case None =>
              existingValue
          }
        key -> v
    } ++ newKeys.map {
      case (key, value) =>
        (key -> List(value))
    }

    this.contextThreadLocal.set(newContext)
  }

  /** Pops an old value from the stack for the named thread attribute.  The expected value must be specified and, if
    * it does not match the value on top of the stack, will throw an exception.  This is to protect against
    * programming errors where the `push` and `pop` calls don't match up.
    *
    * @param name the name of the attribute for which to pop a value
    * @param value the expected value to pop for the named attribute
    */
  def pop(name: String, value: String): Unit = pop(Map(name -> value))

  /** Pops a collection of old values from the stacks for the named thread attributes.  The expected values must
    * match the values on tops of the stacks, or else an exception will be thrown.  This is to protect against
    * programming errors where the `push` and `pop` calls don't match up.
    *
    * @param attributes a map of the thread attribute names to pop and the expected value for each
    */
  def pop(attributes: Map[String, String]): Unit = {
    val context = this.contextThreadLocal.get

    // Check to make sure that all the removed keys are valid (that they're are the top of the stacks)

    attributes.foreach {
      case (k, v) =>
        context.get(k) match {
          case Some(stack) =>
            if (stack.head != v)
              throw new IllegalStateException(
                "trying to pop (" + k + " -> " + v + ") from the logging context, but that name maps to '" + stack.head + "'"
              )
          case None =>
            throw new IllegalStateException(
              "trying to pop (" + k + " -> " + v + ") from the logging context, but that name isn't there"
            )
        }
    }

    val newContext = context.flatMap {
      case (key, existingValue) =>
        attributes.get(key) match {
          case Some(newValue) =>
            val stack = existingValue.tail
            if (stack.nonEmpty)
              Some(key -> stack)
            else
              None
          case None =>
            Some(key -> existingValue)
        }
    }

    this.contextThreadLocal.set(newContext)
  }

  /** Removes all thread attributes.  This includes all stacked values.
    */
  def clear: Unit = this.contextThreadLocal.remove()

  /** Executes a thunk, during which the specified thread attribute will be in place.  After the thunk has completed,
    * the thread attribute is returned to its original value (before the call to `during`).  The return value of
    * the call is the return value of the thunk.
    *
    * @param name the name of the thread attribute to set
    * @param value the value of the named thread attribute
    * @param fn the thunk to execute
    * @tparam A the return type of the thunk
    * @return the return value of the executed thunk
    */
  def during[A](name: String, value: String)(fn: => A): A = during(Map(name -> value))(fn)

  /** Executes a thunk, during which the specified thread attributes will be in place.  After the thunk has completed,
    * the thread attributes are returned to their original values (before the call to `during`).  The return value of
    * the call is the return value of the thunk.
    *
    * @param attributes the thread attribute names and values to push during the thunk execution
    * @param fn the thunk to execute
    * @tparam A the return type of the thunk
    * @return the return value of the executed thunk
    */
  def during[A](attributes: Map[String, String])(fn: => A): A = {
    push(attributes)
    try {
      fn
    } finally {
      pop(attributes)
    }
  }
}
