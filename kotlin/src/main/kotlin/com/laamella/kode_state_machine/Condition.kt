package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.condition.AfterCondition
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.condition.NeverCondition

/**
 * A way to define a condition that is met or not.
 *
 * @param E event type.
 */
interface Condition<E> {
    /**
     * Handle an event.
     *
     * @param event the event that has occurred.
     */
    fun handleEvent(event: E)

    /**
     * @return whether the condition is met.
     */
	val isMet: Boolean

    /**
     * This method is called every time the sourceState for this transition is
     * entered. It can be used to implement stateful transitions, like
     * transitions that fire after a certain amount of time.
     */
    fun reset()
}

fun <E> always(): Condition<E> {
    return AlwaysCondition()
}

fun <E> never(): Condition<E> {
    return NeverCondition()
}

fun <E> after(milliseconds: Long): Condition<E> {
    return AfterCondition(milliseconds)
}
