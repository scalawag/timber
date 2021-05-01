// timber -- Copyright 2012-2015 -- Justin Patterson
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

package org.scalawag.timber.backend

import java.util.concurrent.{LinkedBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit}

import scala.concurrent.ExecutionContext

// Has at most one thread that will handle entry dispatch.  This number can drop to zero if there's no work for half
// a second.  These threads are not daemon threads.  The idea is that the logging system will keep the JVM alive to
// make sure that it has had a chance to write all of the entries to disk before exit.  It will only do so for half
// a second though.  That should be enough time for anything that's already been logged to make it into the queue
// and trigger thread creation, thus keeping the JVM alive with a non-daemon thread.  Once all of the entries have
// been received, the thread will die and, if that was the only thread left in the JVM, it will be exit.

private[backend] object SingleThreadExecutionContext {
  private[this] class NamedThreadFactory(name: String) extends ThreadFactory {
    def newThread(r: Runnable): Thread = new Thread(r, name)
  }

  def apply(threadName: String) =
    ExecutionContext.fromExecutor(
      new ThreadPoolExecutor(
        0,
        1,
        500L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable],
        new NamedThreadFactory(threadName)
      )
    )
}
