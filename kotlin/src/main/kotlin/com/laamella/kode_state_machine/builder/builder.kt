package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.Action
import com.laamella.kode_state_machine.Condition
import com.laamella.kode_state_machine.Conditions
import com.laamella.kode_state_machine.StateMachine
import com.laamella.kode_state_machine.action.LogAction
import com.laamella.kode_state_machine.builder.SimpleState.*
import com.laamella.kode_state_machine.condition.AfterCondition
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.condition.MultiEventMatchCondition
import com.laamella.kode_state_machine.condition.NeverCondition
import com.laamella.kode_state_machine.condition.SingleEventMatchCondition

class StateMachineBuilder<T, E, P : Comparable<P>>(private val defaultPriority: P) {
    val stateBuilders = arrayListOf<StateBuilder<T, E, P>>()

    fun state(state: T, init: StateBuilder<T, E, P> .() -> Unit): StateBuilder<T, E, P> {
        return states(state, init = init)
    }

    fun states(vararg states: T, init: StateBuilder<T, E, P> .() -> Unit): StateBuilder<T, E, P> {
        val stateBuilder = StateBuilder<T, E, P>(*states)
        stateBuilders.add(stateBuilder)
        stateBuilder.init()
        return stateBuilder
    }

    fun build(): StateMachine<T, E, P> {
        return StateMachine()
    }

    fun more(more: StateMachineBuilder<T, E, P>.() -> Unit): StateMachineBuilder<T, E, P> {
        this.more()
        return this
    }
}

class StateBuilder<T, E, P : Comparable<P>>(vararg states: T) {
    fun transitionsTo(
        vararg to: T,
        condition: Condition<E> = AlwaysCondition(),
        action: Action? = null,
        priority: P? = null
    ) {
        TODO("Not yet implemented")
    }

    fun areStartStates() {
        TODO("Not yet implemented")
    }

    fun areEndStates() {

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

    fun <E> isEvent(vararg events: E): Conditions<E> {
        assert(events.isNotEmpty())

        if (events.size == 1) {
            val singleEvent: E = events[0]
            return Conditions(SingleEventMatchCondition(singleEvent))
        }

        return Conditions(MultiEventMatchCondition(*events))
    }

    fun log(logText: String): Action {
        return LogAction(logText)
    }

    fun isAStartState() {
        areStartStates()
    }

    fun isAnEndState() {
        areEndStates()
    }
}

fun <T, E, P : Comparable<P>> stateMachine(
    defaultPriority: P,
    init: StateMachineBuilder<T, E, P>.() -> Unit
): StateMachineBuilder<T, E, P> {
    val stateMachine = StateMachineBuilder<T, E, P>(defaultPriority)
    stateMachine.init()
    return stateMachine
}

enum class SimpleState { A, B, C, D }

val x = stateMachine<SimpleState, Any, Int>(0) {
    states(A, B) {
        areStartStates()
        transitionsTo(C)
        transitionsTo(C, condition = never(), action = log("gwrgw"), priority = 5)
    }
    states(C, D) {
        areEndStates()
    }
}.build()
