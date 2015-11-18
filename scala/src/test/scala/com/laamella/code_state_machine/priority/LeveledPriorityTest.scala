package com.laamella.code_state_machine.priority

import com.laamella.code_state_machine.UnitSpec
import com.laamella.code_state_machine.priority.LeveledPriority._

import scala.collection.mutable
import org.scalatest.Assertions._

class LeveledPriorityTest extends UnitSpec {
  "leveled priorities" should "have the correct ordering" in {
    val q = new mutable.PriorityQueue[LeveledPriority]()
    q.enqueue(LOWEST, HIGH, NORMAL, HIGHEST, LOW)

    assert(HIGHEST == q.dequeue())
    assert(HIGH == q.dequeue())
    assert(NORMAL == q.dequeue())
    assert(LOW == q.dequeue())
    assert(LOWEST == q.dequeue())
  }
}