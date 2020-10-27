package com.laamella.code_state_machine.condition;

import com.laamella.code_state_machine.StateMachine;

/**
 * A condition that acts as a kind of sub-statemachine. The condition is met
 * when the embedded statemachine has no active states left.
 *
 * @param <E> event type. The same type as the parent state machine.
 */
// TODO test
public final class SubStateMachineCondition<T, E, P extends Comparable<P>> extends EventBasedCondition<E> {
    private final StateMachine<T, E, P> stateMachine;

    /**
     * @param stateMachine the state machine to use. Note that using the same state
     *                     machine for multiple conditions will not magically clone it,
     *                     it still is the same machine with the same state in all
     *                     conditions.
     */
    public SubStateMachineCondition(StateMachine<T, E, P> stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    protected boolean conditionIsMetAfterHandlingEvent(E event) {
        stateMachine.handleEvent(event);
        return stateMachine.isFinished();
    }

    @Override
    public void reset() {
        super.reset();
        stateMachine.reset();
    }
}
