package com.laamella.kode_state_machine.condition

/**
 * This condition is always met.
 */
class AlwaysCondition<E> : NonEventBasedCondition<E>() {
    override val isMet: Boolean
        get() = true

    override fun toString(): String {
        return "always"
    }
}