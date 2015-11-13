package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.StateMachine

/**
 * This condition is met when all states passed in the constructor are active.
 */
final class StatesInactiveCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P] , statesThatMustBeInactive: T*) extends NonEventBasedCondition[E] {
	override def isMet: Boolean = {
		for (stateThatMustBeInactive <- statesThatMustBeInactive) {
			if (stateMachine.getActiveStates.contains(stateThatMustBeInactive)) {
				return false
			}
		}
		true
	}
}
