package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.Condition

/**
 * A base class for conditions that are met depending on some kind of event
 * handling.
 */
abstract class EventBasedCondition[E] extends Condition[E] {
  private var met = false

  override def isMet = met

  override def reset() = met = false

  override def handleEvent(event: E) = {
    if (!met && conditionIsMetAfterHandlingEvent(event)) {
      met = true
    }
  }

  protected def conditionIsMetAfterHandlingEvent(event: E): Boolean
}
