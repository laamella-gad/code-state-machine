package com.laamella.code_state_machine.condition

/**
 * This condition is always met.
 */
final class AlwaysCondition[E] extends NonEventBasedCondition[E] {
	/** @return whether the condition is met. */
	override def isMet: Boolean = true

	override def toString = "always"
}