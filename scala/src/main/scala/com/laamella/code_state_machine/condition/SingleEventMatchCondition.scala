package com.laamella.code_state_machine.condition

/**
 * This condition is met when the event is equal to the event passed in the
 * constructor.
 */
final class SingleEventMatchCondition[E](singleEvent: E) extends EventBasedCondition[E] {

  override def toString = s"is $singleEvent"

  override protected def conditionIsMetAfterHandlingEvent(event: E) = singleEvent equals event
}
