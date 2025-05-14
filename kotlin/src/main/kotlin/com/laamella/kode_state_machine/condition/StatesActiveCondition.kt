package com.laamella.kode_state_machine.condition

import com.laamella.kode_state_machine.StateMachine
import java.util.*

/**
 * This condition is met when all states passed in the constructor are active.
 */
class StatesActiveCondition<T, E, P : Comparable<P>>(
    private val stateMachine: StateMachine<T, E, P>,
    vararg statesThatMustBeActive: T
) : NonEventBasedCondition<E>() {
    private val statesThatMustBeActive: HashSet<T> = HashSet<T>(Arrays.asList<T>(*statesThatMustBeActive))

    override val isMet: Boolean
        get() = stateMachine.activeStates.containsAll(statesThatMustBeActive)
}