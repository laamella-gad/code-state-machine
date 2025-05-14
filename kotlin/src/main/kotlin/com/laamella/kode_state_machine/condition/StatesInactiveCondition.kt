package com.laamella.kode_state_machine.condition

import com.laamella.kode_state_machine.StateMachine

/**
 * This condition is met when all states passed in the constructor are active.
 */
class StatesInactiveCondition<T, E, P : Comparable<P>>(
    private val stateMachine: StateMachine<T, E, P>,
    private vararg val statesThatMustBeInactive: T
) : NonEventBasedCondition<E>() {
    override val isMet: Boolean
        get() {
            for (stateThatMustBeInactive in statesThatMustBeInactive) {
                if (stateMachine.activeStates.contains(stateThatMustBeInactive)) {
                    return false
                }
            }
            return true
        }
}