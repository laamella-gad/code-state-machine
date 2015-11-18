package com.laamella.code_state_machine.priority

import com.laamella.code_state_machine.priority.LeveledPriority._
import org.junit.Assert._
import org.junit.{Assert, Test}

import scala.collection.mutable

class LeveledPriorityTest {
  @Test
  def testOrder() = {
    val q= new mutable.PriorityQueue[LeveledPriority]()
    q.enqueue(LOWEST, HIGH, NORMAL, HIGHEST, LOW)

    assertEquals(HIGHEST, q.dequeue())
    assertEquals(HIGH, q.dequeue())
    assertEquals(NORMAL, q.dequeue())
    assertEquals(LOW, q.dequeue())
    assertEquals(LOWEST, q.dequeue())
  }
}