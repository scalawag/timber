package org.scalawag.timber.impl

import java.util.concurrent.ThreadFactory

class NamedThreadFactory(name:String) extends ThreadFactory {
  def newThread(r:Runnable):Thread = new Thread(r,name)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
