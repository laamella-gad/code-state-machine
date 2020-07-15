package com.laamella.code_state_machine.condition;

/**
 * This condition is never met, and as such blocks a transition from ever
 * firing. Probably only useful in test scenarios.
 */
public final class NeverCondition<E> extends NonEventBasedCondition<E> {
    @Override
    public boolean isMet() {
        return false;
    }

    @Override
    public String toString() {
        return "never";
    }
}