package com.laamella.code_state_machine

import com.laamella.code_state_machine.util.Chain

import scala.collection.mutable.ListBuffer

/** A simple wrapper around a list of conditions. */
class Conditions[E](conditions: ListBuffer[Condition[E]]) extends Chain[Condition[E]](conditions) {
  def this(conditions: Condition[E]*) = this(new ListBuffer ++= conditions.toTraversable)

  def handleEvent(event: E) {
    for (condition <- getItems) {
      condition.handleEvent(event)
    }
  }

  /** @return true if all conditions are met, else false. */
  def isMet: Boolean = {
    for (condition <- getItems) {
      if (!condition.isMet) {
        return false
      }
    }
    true
  }

  def reset() {
    for (condition <- getItems) {
      condition.reset()
    }
  }
}
