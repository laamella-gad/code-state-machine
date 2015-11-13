package com.laamella.code_state_machine

/**
 * A way to define a condition that is met or not.
 *
 * E is the event type.
 */
trait Condition[E] {
  /**
   * Handle an event.
   *
   * @param event the event that has occurred.
   */
  def handleEvent(event: E)

  /**
   * @return whether the condition is met.
   */
  def isMet: Boolean

  /**
   * This method is called every time the sourceState for this transition is
   * entered. It can be used to implement stateful transitions, like
   * transitions that fire after a certain amount of time.
   */
  def reset()
}
