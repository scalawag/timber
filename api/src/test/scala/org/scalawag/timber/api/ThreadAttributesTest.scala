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

package org.scalawag.timber.api

import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import java.util.concurrent.CyclicBarrier
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Await._
import collection.immutable.Stack

class ThreadAttributesTest extends FunSpec with Matchers with BeforeAndAfter {

  before {
    ThreadAttributes.clear
  }

  describe("general") {

    it("should start empty") {
      ThreadAttributes.get shouldBe Map()
    }

    it("should maintain separate contexts for separate threads") {
      import scala.concurrent.ExecutionContext.Implicits.global

      val barrier = new CyclicBarrier(2)

      val f1 = Future {
        ThreadAttributes.push("ip","127.0.0.1")
        barrier.await
        ThreadAttributes.get.get("ip").head shouldBe "127.0.0.1"
        barrier.await
      }

      val f2 = Future {
        ThreadAttributes.push("ip","127.0.0.2")
        barrier.await
        ThreadAttributes.get.get("ip").head shouldBe "127.0.0.2"
        barrier.await
      }

      ready(f1,Duration.Inf)
      ready(f2,Duration.Inf)
    }

  }

  describe("push(String,String") {

    it("should push a value onto the stack") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
    }

    it("should keep pushes with different keys distinct") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.push("b","2")
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"),"b" -> Stack("2"))
    }

    it("should stack pushes with the same name") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.push("a","2")
      ThreadAttributes.get shouldBe Map("a" -> Stack("2","1"))
    }

  }

  describe("push(Map") {

    it("should push a Map of values onto the stack") {
      ThreadAttributes.push(Map("a" -> "1","b" -> "2"))
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"),"b" -> Stack("2"))
    }

    it("should support pushing overlapping Maps") {
      ThreadAttributes.push(Map("a" -> "1","b" -> "2"))
      ThreadAttributes.push(Map("a" -> "3","c" -> "4"))
      ThreadAttributes.get shouldBe Map("a" -> Stack("3","1"),"b" -> Stack("2"),"c" -> Stack("4"))
    }

  }

  describe("getTopmost") {

    it("should retrieve the single value") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.getTopmost shouldBe Map("a" -> "1")
    }

    it("should retrieve the topmost value of multiple") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.push("a","2")
      ThreadAttributes.getTopmost shouldBe Map("a" -> "2")
    }

    it("should retrieve nothing when there are no values") {
      ThreadAttributes.getTopmost shouldBe Map.empty
    }

  }

  describe("pop(String,String)") {

    it("should remove the innermost value") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.pop("a","1")
      ThreadAttributes.get shouldBe Map()
    }

    it("should remove the innermost value and leave any remains") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.push("a","2")
      ThreadAttributes.pop("a","2")
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
    }

    it("should not affect other keys") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.push("b","2")
      ThreadAttributes.pop("a","1")
      ThreadAttributes.get shouldBe Map("b" -> Stack("2"))
    }

    it("should fail to remove name from empty context") {
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop("a","1"))
    }

    it("should fail to remove nonexistent name") {
      ThreadAttributes.push("a","1")
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop("b","1"))
    }

    it("should fail to remove the wrong value") {
      ThreadAttributes.push("a","1")
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop("a","2"))
    }

  }

  describe("pop(Map)") {

    it("should remove the innermost value and leave any remains") {
      ThreadAttributes.push(Map("a" -> "1"))
      ThreadAttributes.push(Map("a" -> "2","b" -> "3"))
      ThreadAttributes.pop(Map("a" -> "2","b" -> "3"))
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
    }

    it("should fail to remove name from empty context") {
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop(Map("a" -> "1")))
    }

    it("should fail to remove nonexistent name") {
      ThreadAttributes.push(Map("a" -> "1"))
      ThreadAttributes.push(Map("a" -> "2","b" -> "3"))
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop("c","1"))
    }

    it("should fail to remove the wrong value") {
      ThreadAttributes.push(Map("a" -> "1"))
      ThreadAttributes.push(Map("a" -> "2","b" -> "3"))
      an [IllegalStateException] shouldBe thrownBy (ThreadAttributes.pop("a","1"))
    }

  }

  describe("during(String,String)") {

    it("should set a name's value for the duration of a block") {
      ThreadAttributes.during("a","1") {
        ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
      }
      ThreadAttributes.get shouldBe Map()
    }

    it("should reset the context to the state prior to the block") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.during("a","2") {
        ThreadAttributes.get shouldBe Map("a" -> Stack("2","1"))
      }
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
    }

  }

  describe("during(Map)") {
    it("should set multiple name's value for the duration of a block") {
      ThreadAttributes.push("a","1")
      ThreadAttributes.during(Map("a" -> "2","b" -> "3")) {
        ThreadAttributes.get shouldBe Map("a" -> Stack("2","1"),"b" -> Stack("3"))
      }
      ThreadAttributes.get shouldBe Map("a" -> Stack("1"))
    }
  }

  describe("clear") {
    it("should clear all values") {
      ThreadAttributes.push(Map("a" -> "1"))
      ThreadAttributes.push(Map("a" -> "2","b" -> "3"))
      ThreadAttributes.clear
      ThreadAttributes.get shouldBe Map.empty
    }
  }
}

