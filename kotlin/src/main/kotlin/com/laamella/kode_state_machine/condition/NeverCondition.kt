package com.laamella.kode_state_machine.condition

/**
 * This condition is never met, and as such blocks a transition from ever
 * firing. Probably only useful in test scenarios.
 */
class NeverCondition<E> : NonEventBasedCondition<E>() {
    override val isMet: Boolean
        get() = false

    override fun toString(): String {
        return "never"
    }
}