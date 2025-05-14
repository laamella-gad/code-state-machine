package com.laamella.kode_state_machine.condition

/**
 * This condition is met when the event is equal to the event passed in the
 * constructor.
 */
class SingleEventMatchCondition<E>(private val singleEvent: E) : EventBasedCondition<E>() {
    override fun toString(): String {
        return "is $singleEvent"
    }

    override fun conditionIsMetAfterHandlingEvent(event: E): Boolean {
        return singleEvent == event
    }
}