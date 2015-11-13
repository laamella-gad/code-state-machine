package com.laamella.code_state_machine.condition

/**
 * This condition is met when the event is equal to one of the events passed in
 * the constructor.
 */
final class MultiEventMatchCondition[E](matchEvents: E*) extends EventBasedCondition[E] {
  override def toString = {
    val str = new StringBuilder("one of (")
    for (matchEvent <- matchEvents) {
      str.append(matchEvent.toString).append(" ")
    }
    str.append(")").toString()
  }

  override protected def conditionIsMetAfterHandlingEvent(event: E) = matchEvents contains event
}
