package com.laamella.code_state_machine.action

import com.laamella.code_state_machine.Condition

/**
 * An action which finishes at some time in the future. A transition can wait
 * for the action to be finished by using the finished condition.
 */
trait FinishableAction[Event] extends (() => Unit) {
	def finished: Condition[Event]
}
