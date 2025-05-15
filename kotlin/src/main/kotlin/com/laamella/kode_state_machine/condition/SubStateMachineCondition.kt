package com.laamella.kode_state_machine.condition

import com.laamella.kode_state_machine.StateMachine

/**
 * A condition that acts as a kind of sub-statemachine. The condition is met
 * when the embedded statemachine has no active states left.
 *
 * @param stateMachine the state machine to use. Note that using the same state
 * machine for multiple conditions will not magically clone it,
 * it still is the same machine with the same state in all
 * conditions.
 * @param <E> event type. The same type as the parent state machine.
</E> */
// TODO test
class SubStateMachineCondition<T, E, P : Comparable<P>>(
    private val stateMachine: StateMachine<T, E, P>
) :
    EventBasedCondition<E>() {
    override fun conditionIsMetAfterHandlingEvent(event: E): Boolean {
        stateMachine.handleEvent(event)
        return stateMachine.isFinished()
    }

    override fun reset() {
        super.reset()
        stateMachine.reset()
    }
}
