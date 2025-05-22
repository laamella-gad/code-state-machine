package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.condition.MultiEventMatchCondition
import com.laamella.kode_state_machine.condition.SingleEventMatchCondition

fun <E> isEvent(event: E): Condition<E> {
    return SingleEventMatchCondition(event)
}

fun <E> isEventOneOf(vararg events: E): MultiEventMatchCondition<E> {
    return MultiEventMatchCondition(*events)
}

