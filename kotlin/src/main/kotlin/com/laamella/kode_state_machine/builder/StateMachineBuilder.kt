package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.*
import com.laamella.kode_state_machine.condition.AlwaysCondition
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

class TransitionBuilder<T, E, P : Comparable<P>>() {
    val to = mutableListOf<T>()
    val condition = mutableListOf<Condition<E>>()
    val action = mutableListOf<Action>()
    var priority: P? = null

    fun to(vararg destinationStates: T) {
        to.addAll(destinationStates)
    }

    fun condition(condition: Condition<E>) {
        this.condition.add(condition)
    }

    fun onEvent(event: E) {
        condition(isEvent(event))
    }

    fun onEvents(vararg events: E) {
        condition(isEventOneOf(*events))
    }

    fun action(action: Action) {
        this.action.add(action)
    }

    fun priority(value: P) {
        this.priority = value
    }

    fun build(source: T, defaultPriority: P): List<Transition<T, E, P>> {
        return to.map { destination ->
            Transition(
                source,
                destination,
                condition,
                priority ?: defaultPriority,
                action
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
        to: T,
        condition: Condition<E> = AlwaysCondition(),
        action: Action = NoAction(),
        priority: P? = null
    ) {
        transition {
            to(to)
            condition(condition)
            action(action)
            if (priority != null) priority(priority)
        }
    }

    fun transition(init: TransitionBuilder<T, E, P> .() -> Unit): TransitionBuilder<T, E, P> {
        val transitionBuilder = TransitionBuilder<T, E, P>()
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
