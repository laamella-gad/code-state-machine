package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.Condition

/**
 * A base class for conditions that do not respond to events.
 */
abstract class NonEventBasedCondition[E] extends Condition[E] {
	// Not event based, so not used.
	override def handleEvent(event: E): Unit = Unit

	// Does nothing by default.
	override def reset(): Unit = Unit
}
