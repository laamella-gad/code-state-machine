package com.laamella.code_state_machine.condition;

/**
 * This condition is met after a certain amount of milliseconds.
 */
class AfterCondition[E](milliseconds: Long) extends NonEventBasedCondition[E] {
	private var minimalMeetTime: Long = _

	override def isMet = System.currentTimeMillis() > minimalMeetTime

	override def reset() = minimalMeetTime = System.currentTimeMillis() + milliseconds
}