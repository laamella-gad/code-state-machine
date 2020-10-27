package com.laamella.code_state_machine.condition;

import java.util.Arrays;
import java.util.HashSet;

import com.laamella.code_state_machine.StateMachine;

/**
 * This condition is met when all states passed in the constructor are active.
 */
public final class StatesActiveCondition<T, E, P extends Comparable<P>> extends NonEventBasedCondition<E> {
    private final HashSet<T> statesThatMustBeActive;
    private final StateMachine<T, E, P> stateMachine;

    @SafeVarargs
    public StatesActiveCondition(StateMachine<T, E, P> stateMachine, T... statesThatMustBeActive) {
        this.stateMachine = stateMachine;
        this.statesThatMustBeActive = new HashSet<>(Arrays.asList(statesThatMustBeActive));
    }

    @Override
    public boolean isMet() {
        return stateMachine.getActiveStates().containsAll(statesThatMustBeActive);
    }
}