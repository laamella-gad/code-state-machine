package com.laamella.kode_state_machine.condition

import java.util.*

/**
 * This condition is met when the event is equal to one of the events passed in
 * the constructor.
 */
class MultiEventMatchCondition<E>(vararg events: E) : EventBasedCondition<E>() {
    private val matchEvents: MutableSet<E> = HashSet<E>(Arrays.asList<E>(*events))

    override fun toString(): String {
        val str = StringBuilder("one of (")
        for (matchEvent in matchEvents) {
            str.append(matchEvent.toString()).append(" ")
        }
        return str.append(")").toString()
    }

    override fun conditionIsMetAfterHandlingEvent(event: E): Boolean {
        return matchEvents.contains(event)
    }
}