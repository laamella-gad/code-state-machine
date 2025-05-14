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
    private val startStates: MutableSet<T> = HashSet<T>()
    private val endStates: MutableSet<T> = HashSet<T>()

    /**
     * @return a set of all active states.
     */
    @JvmField
    val activeStates: MutableSet<T> = HashSet<T>()
    private val exitEvents: MutableMap<T, Actions> = HashMap<T, Actions>()
    private val entryEvents: MutableMap<T, Actions> = HashMap<T, Actions>()
    private val transitions: MutableMap<T, Queue<Transition<T, E, P>>> = HashMap<T, Queue<Transition<T, E, P>>>()

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
        if (startStates.size == 0) {
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
    fun isActive(state: T?): Boolean {
        return activeStates.contains(state)
    }

    val isFinished: Boolean
        /**
         * @return whether no states are active. Can be caused by all active states
         * having disappeared into end states, or by having no start states
         * at all.
         */
        get() = activeStates.size == 0

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
                transition.condition.handleEvent(event)
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
        val transitionsThatHaveFiredBefore = HashSet<Transition<T, E, P>>()

        do {
            stillNewTransitionsFiring = false
            val statesToExit = HashSet<T>()
            val transitionsToFire = HashSet<Transition<T, E, P>>()
            val statesToEnter = HashSet<T>()

            for (sourceState in activeStates) {
                var firingPriority: P? = null
                for (transition in findTransitionsForState(sourceState)!!) {
                    if (!transitionsThatHaveFiredBefore.contains(transition)) {
                        if (firingPriority != null && transition.priority != firingPriority) {
                            // We reached a lower prio while higher prio transitions are firing.
                            // Don't consider these anymore, go to the next source state.
                            break
                        }
                        if (transition.condition.isMet) {
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
        if (actions != null) {
            actions.execute()
        }
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
            if (activeStates.size == 0) {
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
        for (transition in transitions.get(sourceState)!!) {
            transition.condition.reset()
        }
    }

    private fun findTransitionsForState(sourceState: T): Queue<Transition<T, E, P>>? {
        return transitions.get(sourceState)
    }

    private fun executeExitActions(state: T?) {
        executeActions(exitEvents.get(state))
    }

    private fun executeEntryActions(state: T?) {
        executeActions(entryEvents.get(state))
    }

    /**
     * Gives access to the internals of the state machine.
     */
    inner class Internals {
        /**
         * @return the end states.
         */
        fun getEndStates(): MutableSet<T> {
            return HashSet<T>(this@StateMachine.endStates)
        }

        /**
         * @return the start states.
         */
        fun getStartStates(): MutableSet<T> {
            return HashSet<T>(this@StateMachine.startStates)
        }

        val sourceStates: MutableSet<T>
            /**
             * @return the states that have outgoing transitions defined.
             */
            get() = HashSet<T>(this@StateMachine.transitions.keys)

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
                transition.destinationState, transition.condition, transition.actions
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
