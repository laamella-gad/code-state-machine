package com.laamella.code_state_machine.condition;

/**
 * This condition is met when the event is equal to the event passed in the
 * constructor.
 */
public final class SingleEventMatchCondition<E> extends EventBasedCondition<E> {
    private final E singleEvent;

    public SingleEventMatchCondition(E singleEvent) {
        this.singleEvent = singleEvent;
    }

    @Override
    public String toString() {
        return "is " + singleEvent;
    }

    @Override
    protected boolean conditionIsMetAfterHandlingEvent(E event) {
        return singleEvent.equals(event);
    }
}