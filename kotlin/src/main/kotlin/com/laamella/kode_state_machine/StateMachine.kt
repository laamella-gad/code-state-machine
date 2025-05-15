package com.laamella.kode_state_machine

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

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
 *  * It does not have sub state machines; a state machine is not a state.
 *  * It has transitions that use a state machine for their condition.
 *  * With the DSL, transitions to a certain state can be added for multiple
 * source states, thereby faking global transitions.
 *  * It tries to put as few constraints as possible on the user.
 *  * It has only one dependency: slf4j for logging, which can be configured to
 * use any other logging framework.
 *  * The state type can be anything.
 *  * The event type can be anything.
 *  * The priority type can be anything as long as it's Comparable.
 *  * It has two, always accessible modes of usage: asking the state machine
 * for the current state, or having the state machine trigger actions that
 * change the user code state.
 *
 *
 * @param <T> State type. Each state should have a single instance of this type.
 * An enum is a good fit.
 * @param <E> Event type. Events come into the state machine from the outside
 * world, and are used to trigger state transitions.
 * @param <P> Priority type. Will be used to give priorities to transitions.
 * Enums and Integers are useful here.
 */
class StateMachine<T, E, P : Comparable<P>> {
    private val startStates = mutableSetOf<T>()
    private val endStates = mutableSetOf<T>()

    /**
     * @return a set of all active states.
     */
    val activeStates =mutableSetOf<T>()
    private val exitEvents = mutableMapOf<T, Actions>()
    private val entryEvents = mutableMapOf<T, Actions>()
    private val transitions= mutableMapOf<T, Queue<Transition<T, E, P>>>()

    /**
     * Create a new, empty state machine. To fill it, use the internals, or use
     * one of the builders.
     */
    init {
        log.debug("New Machine")
    }

    /**
     * Resets all active states to the start states.
     */
    fun reset() {
        log.debug("reset()")
        if (startStates.isEmpty()) {
            log.warn("State machine does not contain any start states.")
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
        log.debug("handle event {}", event)

        for (sourceState in activeStates) {
            for (transition in findTransitionsForState(sourceState)!!) {
                transition.conditions.handleEvent(event)
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
                for (transition in findTransitionsForState(sourceState)!!) {
                    if (!transitionsThatHaveFiredBefore.contains(transition)) {
                        if (firingPriority != null && transition.priority != firingPriority) {
                            // We reached a lower prio while higher prio transitions are firing.
                            // Don't consider these anymore, go to the next source state.
                            break
                        }
                        if (transition.conditions.isMet) {
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

    private fun executeActions(actions: Actions?) {
        actions?.execute()
    }

    private fun exitState(state: T) {
        log.debug("exit state {}", state)
        if (activeStates.contains(state)) {
            executeExitActions(state)
            activeStates.remove(state)
        }
    }

    private fun enterState(newState: T) {
        if (endStates.contains(newState)) {
            log.debug("enter end state {}", newState)
            executeEntryActions(newState)
            if (activeStates.isEmpty()) {
                log.debug("machine is finished")
            }
            return
        }
        if (activeStates.add(newState)) {
            log.debug("enter state {}", newState)
            executeEntryActions(newState)
            resetTransitions(newState)
        }
    }

    private fun resetTransitions(sourceState: T) {
        transitions[sourceState]?.forEach { transition ->
            transition.conditions.reset()
        }
    }

    private fun findTransitionsForState(sourceState: T): Queue<Transition<T, E, P>>? {
        return transitions[sourceState]
    }

    private fun executeExitActions(state: T?) {
        executeActions(exitEvents[state])
    }

    private fun executeEntryActions(state: T?) {
        executeActions(entryEvents[state])
    }

    /**
     * Gives access to the internals of the state machine.
     */
    inner class Internals {
        fun getEndStates(): Set<T> = this@StateMachine.endStates
        fun getStartStates(): Set<T> = this@StateMachine.startStates
        fun getSourceStates(): Set<T> = this@StateMachine.transitions.keys

        /**
         * @return the outgoing transitions for a source state.
         */
        fun getTransitionsForSourceState(sourceState: T): Queue<Transition<T, E, P>>? {
            return this@StateMachine.findTransitionsForState(sourceState)
        }

        // TODO complete meta information
        /**
         * Add 0 or more actions to be executed when the state is exited.
         */
        fun addExitActions(state: T, vararg action: Action) {
            log.debug("Create exit action for '{}' ({}) ", state, action)
            exitEvents.computeIfAbsent(state) { t -> Actions() }.add(*action)
        }

        /**
         * Add 0 or more actions to be executed when the state is entered.
         */
        fun addEntryActions(state: T, vararg action: Action) {
            log.debug("Create entry action for '{}' ({}) ", state, action)
            entryEvents.computeIfAbsent(state) { t -> Actions() }.add(*action)
        }

        /**
         * Add an end state.
         */
        fun addEndState(endState: T) {
            log.debug("Add end state '{}'", endState)
            endStates.add(endState)
        }

        /**
         * Add a transition.
         */
        fun addTransition(transition: Transition<T, E, P>) {
            val sourceState = transition.sourceState
            log.debug(
                "Create transition from '{}' to '{}' (pre: '{}', action: '{}')", sourceState,
                transition.destinationState, transition.conditions, transition.actions
            )
            transitions.computeIfAbsent(sourceState) { e -> PriorityQueue() }
                .add(transition)
        }

        /**
         * Adds a start state, and immediately activates it.
         */
        fun addStartState(startState: T) {
            log.debug("Add start state '{}'", startState)
            startStates.add(startState)
            activeStates.add(startState)
        }

        val stateMachine: StateMachine<T, E, P>
            /**
             * @return the statemachine whose internals these are.
             */
            get() = this@StateMachine
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StateMachine::class.java)
    }
}
