package com.laamella.code_state_machine.priority

/**
 * By using Integer as the priority type, and assigning priorities by using this
 * class, the state machine will become deterministic since transitions that
 * have been defined earlier always get priority over transitions that have been
 * defined later.
 */
// TODO this is a rather odd solution.

class AutomaticPriority extends Ordered[AutomaticPriority] {
  private val level = PriorityDeterminizer.nextPriority

  override def compare(that: AutomaticPriority): Int = level.compareTo(that.level)
}

private object PriorityDeterminizer {
  var currentPriority: Int = Integer.MAX_VALUE

  def nextPriority: Int = {
    currentPriority -= 1
    currentPriority
  }
}
