package org.scalawag.timber.impl.factory

import scala.collection.immutable.SortedMap
import org.scalawag.timber.impl.InternalLogging

private[factory] class LRUMap[K,V](val maxSize:Int) extends InternalLogging {
  private var nextSerial:Long = 0
  private var byKey:Map[K,Item[K,V]] = null
  private var bySerial:SortedMap[Long,Item[K,V]] = null
  clear

  def get(key:K)(getFn:(K) => V):V = {
    val serial = nextSerial
    nextSerial += 1

    byKey.get(key) match {
      case None =>
        log.debug("cache miss: %s".format(key))
        add(key,getFn(key),serial).value

      case Some(item) =>
        log.debug("cache hit: updating entry %s".format(item))
        update(item,serial).value
    }
  }

  private def add(key:K,value:V,serial:Long) = {
    val item = new Item(key,value,serial)

    log.debug("adding item: %s".format(item))
    byKey += ( item.key -> item )
    bySerial += ( item.serial -> item )

    while ( byKey.size > maxSize ) {
      log.debug("removing excess items")
      removeLeastUsed
    }

    item
  }

  private def update(item:Item[K,V],serial:Long) = {
    val newItem = new Item(item.key,item.value,serial)
    log.debug("updating item: %s to %s".format(item,newItem))
    bySerial -= item.serial
    bySerial += ( newItem.serial -> newItem )
    newItem
  }

  private def remove(item:Item[K,V]) {
    log.debug("removing item: %s".format(item))
    byKey -= item.key
    bySerial -= item.serial
  }

  private def removeLeastUsed {
    remove(bySerial.head._2)
  }

  def clear {
    byKey = Map[K,Item[K,V]]()
    bySerial = SortedMap[Long,Item[K,V]]()
  }

  private case class Item[K,V](val key:K,val value:V,val serial:Long)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
