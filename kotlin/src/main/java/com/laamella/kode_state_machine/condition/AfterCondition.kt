package com.laamella.kode_state_machine.condition

/**
 * This condition is met after a certain amount of milliseconds.
 */
class AfterCondition<E>(private val milliseconds: Long) : NonEventBasedCondition<E>() {
    private var minimalMeetTime: Long = 0

    override val isMet: Boolean
        get() = System.currentTimeMillis() > minimalMeetTime

    override fun reset() {
        minimalMeetTime = System.currentTimeMillis() + milliseconds
    }
}