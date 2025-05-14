package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.util.Chain

/**
 * A simple wrapper around a list of conditions.
 */
class Conditions<E>(vararg conditions: Condition<E>) : Chain<Condition<E>>(*conditions) {

    fun handleEvent(event: E) {
        for (condition in items) {
            condition.handleEvent(event)
        }
    }

    val isMet: Boolean
        /**
         * @return true if all conditions are met, else false.
         */
        get() {
            for (condition in items) {
                if (!condition.isMet) {
                    return false
                }
            }
            return true
        }

    fun reset() {
        for (condition in items) {
            condition.reset()
        }
    }
}
