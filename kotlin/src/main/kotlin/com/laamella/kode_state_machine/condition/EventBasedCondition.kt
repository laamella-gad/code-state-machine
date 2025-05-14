package com.laamella.kode_state_machine.condition

import com.laamella.kode_state_machine.Condition

/**
 * A base class for conditions that are met depending on some kind of event
 * handling.
 */
abstract class EventBasedCondition<E> : Condition<E> {
    override var isMet: Boolean = false

    override fun reset() {
        isMet = false
    }

    override fun handleEvent(event: E) {
        if (!isMet && conditionIsMetAfterHandlingEvent(event)) {
            isMet = true
        }
    }

    /**
     * @param event the event to handle.
     * @return whether the condition is met.
     */
    protected abstract fun conditionIsMetAfterHandlingEvent(event: E): Boolean
}
