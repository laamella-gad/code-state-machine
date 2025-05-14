package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.util.Chain

/**
 * A simple wrapper around a list of conditions.
 */
class Conditions<E> : Chain<Condition<E>> {
    // This method exists only to suppress warnings about varargs.
    constructor() : super()

    @SafeVarargs
    constructor(vararg conditions: Condition<E>) : super(*conditions)

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
