package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.Action
import com.laamella.kode_state_machine.Condition
import com.laamella.kode_state_machine.StateMachine
import com.laamella.kode_state_machine.Transition
import com.laamella.kode_state_machine.action.LogAction
import com.laamella.kode_state_machine.action.NoAction
import com.laamella.kode_state_machine.condition.*
import java.util.*

class StateMachineBuilder<T, E, P : Comparable<P>>(private val defaultPriority: P) {
    val stateBuilders = mutableListOf<StateBuilder<T, E, P>>()

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
        val startStates = mutableSetOf<T>()
        val endStates = mutableSetOf<T>()
        val exitEvents = mutableMapOf<T, MutableList<Action>>()
        val entryEvents = mutableMapOf<T, MutableList<Action>>()
        val transitions = mutableMapOf<T, PriorityQueue<Transition<T, E, P>>>()

        stateBuilders.forEach { sb ->
            sb.build(startStates, endStates, exitEvents, entryEvents, transitions, defaultPriority)
        }

        return StateMachine(startStates, endStates, exitEvents, entryEvents, transitions)
    }

    fun more(more: StateMachineBuilder<T, E, P>.() -> Unit): StateMachineBuilder<T, E, P> {
        this.more()
        return this
    }
}

class TransitionBuilder<T, E, P : Comparable<P>>(
    private val to: List<T>,
    private val condition: Condition<E>,
    private val action: Action,
    private val priority: P?
) {
    fun build(from: T, defaultPriority: P): List<Transition<T, E, P>> {
        // TODO listof!
        return to.map { t ->
            Transition(
                from,
                t,
                listOf(condition),
                priority ?: defaultPriority,
                listOf(action)
            )
        }
    }
}

class StateBuilder<T, E, P : Comparable<P>>(vararg sourceStates: T) {
    private val sourceStates = mutableSetOf(*sourceStates)
    internal var isStartState = false
    private var isEndState = false
    private val exitEvents = mutableListOf<Action>()
    private val entryEvents = mutableListOf<Action>()
    private val transitionBuilders = mutableListOf<TransitionBuilder<T, E, P>>()

    fun transitionsTo(
        vararg to: T,
        condition: Condition<E> = AlwaysCondition(),
        action: Action = NoAction(),
        priority: P? = null
    ) {
        transitionBuilders.add(TransitionBuilder(to.asList(), condition, action, priority))
    }

    fun transition(vararg states: T, init: TransitionBuilder<T, E, P> .() -> Unit): TransitionBuilder<T, E, P> {
        val transitionBuilder = TransitionBuilder<T, E, P>(states.asList(), condition = always(), action = NoAction(), null)
        transitionBuilders.add(transitionBuilder)
        transitionBuilder.init()
        return transitionBuilder
    }

    fun areStartStates() {
        isStartState = true
    }

    fun areEndStates() {
        isEndState = true
    }

    fun isAStartState() {
        areStartStates()
    }

    fun isAnEndState() {
        areEndStates()
    }

    fun onExit(action: Action) {
        exitEvents.add(action)
    }

    fun onEntry(action: Action) {
        entryEvents.add(action)
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

    fun <E> isEvent(vararg events: E): Condition<E> {
        assert(events.isNotEmpty())

        if (events.size == 1) {
            return SingleEventMatchCondition(events[0])
        }

        return MultiEventMatchCondition(*events)
    }

    fun log(logText: String): Action {
        return LogAction(logText)
    }

    fun except(vararg states: T) {
        sourceStates.removeAll(states)
    }

    fun build(
        startStates: MutableSet<T>,
        endStates: MutableSet<T>,
        entryEvents: MutableMap<T, MutableList<Action>>,
        exitEvents: MutableMap<T, MutableList<Action>>,
        transitions: MutableMap<T, PriorityQueue<Transition<T, E, P>>>,
        defaultPriority: P
    ) {
        sourceStates.forEach { ss ->
            if (isStartState) startStates.add(ss)
            if (isEndState) endStates.add(ss)
            entryEvents.computeIfAbsent(ss) { t -> mutableListOf() }.addAll(this.entryEvents)
            exitEvents.computeIfAbsent(ss) { t -> mutableListOf() }.addAll(this.exitEvents)
            transitions.computeIfAbsent(ss) { t -> PriorityQueue<Transition<T, E, P>>() }
                .addAll(this.transitionBuilders.flatMap { t -> t.build(ss, defaultPriority) })
        }
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
