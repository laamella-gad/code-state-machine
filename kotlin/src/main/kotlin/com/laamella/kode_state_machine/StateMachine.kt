package com.laamella.kode_state_machine

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * A programmer friendly state machine.
 *
 * Features:
 *
 *  * It is non-deterministic, but has the tools to become deterministic.
 *  * It allows multiple start states.
 *  * It allows multiple active states.
 *  * It allows multiple end states.
 *  * States and their transitions do not have to form a single graph. Separate
 * graphs may exist inside a single state machine.
 *  * Each state has a chain of entry and exit actions.
 *  * Each transition has a chain of actions.
 *  * It does not do any kind of compilation.
 *  * Its code is written in a straightforward way, and is hopefully easy to
 * understand.
 *  * It has a priority system for transitions.
 *  * It does not have substate machines; a state machine is not a state.
 *  * It has transitions that use a state machine for their condition.
 *  * With the DSL, transitions to a certain state can be added for multiple
 * source states, thereby faking global transitions.
 *  * It tries to put as few constraints as possible on the user.
 *  * It has only one dependency: kotlin-logging-jvm for logging, which can be configured to
 * use any other logging framework.
 *  * The state type can be anything.
 *  * The event type can be anything.
 *  * The priority type can be anything as long as it's Comparable.
 *  * It has two, always accessible modes of usage: asking the state machine
 * for the current state, or having the state machine trigger actions that
 * change the user code state.
 *
 * @param T State type. Each state should have a single instance of this type.
 * An enum is a good fit.
 * @param E Event type. Events come into the state machine from the outside
 * world, and are used to trigger state transitions.
 * @param P Priority type. Will be used to give priorities to transitions.
 * Enums and Integers are useful here.
 */
class StateMachine<T, E, P : Comparable<P>>(
    val startStates: Set<T>,
    val endStates: Set<T>,
    val exitEvents: Map<T, List<Action>>,
    val entryEvents: Map<T, List<Action>>,
    val transitions: Map<T, PriorityQueue<Transition<T, E, P>>>
) {

    /**
     * @return a set of all active states.
     */
    val activeStates = mutableSetOf<T>()

    /**
     * Create a new, empty state machine. To fill it, use the internals, or use
     * one of the builders.
     */
    init {
        logger.debug { "New Machine" }
        reset()
    }

    /**
     * Resets all active states to the start states.
     */
    fun reset() {
        logger.debug { "reset()" }
        if (startStates.isEmpty()) {
            logger.warn { "State machine does not contain any start states." }
        }
        activeStates.clear()
        for (startState in startStates) {
            enterState(startState)
        }
    }

    /**
     * @return whether the state is currently active.
     */
    fun isActive(state: T): Boolean {
        return activeStates.contains(state)
    }

    fun isFinished(): Boolean = activeStates.isEmpty()

    /**
     * Handle an event coming from the user application. After sending the event
     * to all transitions that have an active source state, poll() will be
     * called.
     *
     * @param event some event that has happened.
     */
    fun handleEvent(event: E) {
        logger.debug { "handle event $event" }

        for (sourceState in activeStates) {
            for (transition in transitions[sourceState]!!) {
                transition.conditions.forEach { t -> t.handleEvent(event) }
            }
        }
        poll()
    }

    /**
     * Tells the state machine to look for state changes to execute. This method
     * has to be called regularly, or the state machine will do nothing at all.
     *
     *  * Repeat...
     *
     *  1. For all transitions that have an active source state, find the
     * transitions that will fire.
     *
     *  * Ignore transitions that have already fired in this poll().
     *  * For a single source state, find the transition of the highest
     * priority which will fire (if any fire at all.) If multiple transitions
     * share this priority, fire them all.
     *
     *  1. For all states that will be exited, fire the exit state event.
     *  1. For all transitions that fire, fire the transition action.
     *  1. For all states that will be entered, fire the entry state event.
     *
     *  * ... until no new transitions have fired.
     *
     * This method prevents itself from looping endlessly on a loop in the state
     * machine by only considering transitions that have not fired before in
     * this poll.
     */
    fun poll() {
        var stillNewTransitionsFiring: Boolean
        val transitionsThatHaveFiredBefore = mutableSetOf<Transition<T, E, P>>()

        do {
            stillNewTransitionsFiring = false
            val statesToExit = mutableSetOf<T>()
            val transitionsToFire = mutableSetOf<Transition<T, E, P>>()
            val statesToEnter = mutableSetOf<T>()

            for (sourceState in activeStates) {
                var firingPriority: P? = null
                for (transition in transitions[sourceState]!!) {
                    if (!transitionsThatHaveFiredBefore.contains(transition)) {
                        if (firingPriority != null && transition.priority != firingPriority) {
                            // We reached a lower prio while higher prio transitions are firing.
                            // Don't consider these anymore, go to the next source state.
                            break
                        }
                        if (transition.conditions.all { c -> c.isMet }) {
                            statesToExit.add(sourceState)
                            transitionsToFire.add(transition)
                            statesToEnter.add(transition.destinationState)
                            firingPriority = transition.priority
                        }
                    }
                }
            }

            for (stateToExit in statesToExit) {
                exitState(stateToExit)
            }
            for (transitionToFire in transitionsToFire) {
                executeActions(transitionToFire.actions)
                transitionsThatHaveFiredBefore.add(transitionToFire)
                stillNewTransitionsFiring = true
            }
            for (stateToEnter in statesToEnter) {
                enterState(stateToEnter)
            }
        } while (stillNewTransitionsFiring)
    }

    private fun executeActions(actions: List<Action>?) {
        actions?.forEach { a -> a.execute() }
    }

    private fun exitState(state: T) {
        logger.debug { "exit state $state" }
        if (activeStates.contains(state)) {
            executeExitActions(state)
            activeStates.remove(state)
        }
    }

    private fun enterState(newState: T) {
        if (endStates.contains(newState)) {
            logger.debug {"enter end state $newState"}
            executeEntryActions(newState)
            if (activeStates.isEmpty()) {
                logger.debug {"machine is finished"}
            }
            return
        }
        if (activeStates.add(newState)) {
            logger.debug {"enter state $newState"}
            executeEntryActions(newState)
            resetTransitions(newState)
        }
    }

    private fun resetTransitions(sourceState: T) {
        transitions[sourceState]?.forEach { transition ->
            transition.conditions.forEach { c -> c.reset() }
        }
    }

    private fun executeExitActions(state: T?) {
        executeActions(exitEvents[state])
    }

    private fun executeEntryActions(state: T?) {
        executeActions(entryEvents[state])
    }
}
