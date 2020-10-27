package com.laamella.code_state_machine.condition;

import com.laamella.code_state_machine.StateMachine;

/**
 * This condition is met when all states passed in the constructor are active.
 */
public final class StatesInactiveCondition<T, E, P extends Comparable<P>> extends NonEventBasedCondition<E> {
    private final T[] statesThatMustBeInactive;
    private final StateMachine<T, E, P> stateMachine;

    @SafeVarargs
    public StatesInactiveCondition(StateMachine<T, E, P> stateMachine, T... statesThatMustBeInactive) {
        this.stateMachine = stateMachine;
        this.statesThatMustBeInactive = statesThatMustBeInactive;
    }

    @Override
    public boolean isMet() {
        for (var stateThatMustBeInactive : statesThatMustBeInactive) {
            if (stateMachine.getActiveStates().contains(stateThatMustBeInactive)) {
                return false;
            }
        }
        return true;
    }
}