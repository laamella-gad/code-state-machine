package com.laamella.kode_state_machine.condition

import com.laamella.kode_state_machine.Condition

/**
 * A base class for conditions that do not respond to events.
 */
abstract class NonEventBasedCondition<E> : Condition<E> {
    override fun handleEvent(event: E) {
        // Not event based, so not used.
    }

    override fun reset() {
        // Does nothing by default.
    }
}
