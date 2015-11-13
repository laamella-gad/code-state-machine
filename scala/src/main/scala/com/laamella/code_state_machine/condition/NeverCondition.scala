package com.laamella.code_state_machine.condition

/**
 * This condition is never met, and as such blocks a transition from ever
 * firing. Probably only useful in test scenarios.
 */
final class NeverCondition[E] extends NonEventBasedCondition[E] {
  override def isMet = false

  override def toString = "never"
}