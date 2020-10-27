package com.laamella.code_state_machine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A programmer friendly state machine.
 * <p/>
 * Features:
 * <ul>
 * <li>It is non-deterministic, but has the tools to become deterministic.</li>
 * <li>It allows multiple start states.</li>
 * <li>It allows multiple active states.</li>
 * <li>It allows multiple end states.</li>
 * <li>States and their transitions do not have to form a single graph. Separate
 * graphs may exist inside a single state machine.</li>
 * <li>Each state has a chain of entry and exit actions.</li>
 * <li>Each transition has a chain of actions.</li>
 * <li>It does not do any kind of compilation.</li>
 * <li>Its code is written in a straightforward way, and is hopefully easy to
 * understand.</li>
 * <li>It has a priority system for transitions.</li>
 * <li>It does not have sub state machines; a state machine is not a state.</li>
 * <li>It has transitions that use a state machine for their condition.</li>
 * <li>With the DSL, transitions to a certain state can be added for multiple
 * source states, thereby faking global transitions.</li>
 * <li>It tries to put as few constraints as possible on the user.</li>
 * <li>It has only one dependency: slf4j for logging, which can be configured to
 * use any other logging framework.</li>
 * <li>The state type can be anything.</li>
 * <li>The event type can be anything.</li>
 * <li>The priority type can be anything as long as it's Comparable.</li>
 * <li>It has two, always accessible modes of usage: asking the state machine
 * for the current state, or having the state machine trigger actions that
 * change the user code state.
 * </ul>
 *
 * @param <T> State type. Each state should have a single instance of this type.
 *            An enum is a good fit.
 * @param <E> Event type. Events come into the state machine from the outside
 *            world, and are used to trigger state transitions.
 * @param <P> Priority type. Will be used to give priorities to transitions.
 *            Enums and Integers are useful here.
 */
public class StateMachine<T, E, P extends Comparable<P>> {
    private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

    private final Set<T> startStates = new HashSet<>();
    private final Set<T> endStates = new HashSet<>();
    private final Set<T> activeStates = new HashSet<>();
    private final Map<T, Actions> exitEvents = new HashMap<>();
    private final Map<T, Actions> entryEvents = new HashMap<>();
    private final Map<T, Queue<Transition<T, E, P>>> transitions = new HashMap<>();

    /**
     * Create a new, empty state machine. To fill it, use the internals, or use
     * one of the builders.
     */
    public StateMachine() {
        log.debug("New Machine");
    }

    /**
     * Resets all active states to the start states.
     */
    public void reset() {
        log.debug("reset()");
        if (startStates.size() == 0) {
            log.warn("State machine does not contain any start states.");
        }
        activeStates.clear();
        for (var startState : startStates) {
            enterState(startState);
        }
    }

    /**
     * @return a set of all active states.
     */
    public Set<T> getActiveStates() {
        return activeStates;
    }

    /**
     * @return whether the state is currently active.
     */
    public boolean isActive(T state) {
        return activeStates.contains(state);
    }

    /**
     * @return whether no states are active. Can be caused by all active states
     * having disappeared into end states, or by having no start states
     * at all.
     */
    public boolean isFinished() {
        return activeStates.size() == 0;
    }

    /**
     * Handle an event coming from the user application. After sending the event
     * to all transitions that have an active source state, poll() will be
     * called.
     *
     * @param event some event that has happened.
     */
    public void handleEvent(E event) {
        log.debug("handle event {}", event);

        for (var sourceState : activeStates) {
            for (var transition : findTransitionsForState(sourceState)) {
                transition.getCondition().handleEvent(event);
            }
        }
        poll();
    }

    /**
     * Tells the state machine to look for state changes to execute. This method
     * has to be called regularly, or the state machine will do nothing at all.
     * <ul>
     * <li>Repeat...</li>
     * <ol>
     * <li>For all transitions that have an active source state, find the
     * transitions that will fire.</li>
     * <ul>
     * <li>Ignore transitions that have already fired in this poll().</li>
     * <li>For a single source state, find the transition of the highest
     * priority which will fire (if any fire at all.) If multiple transitions
     * share this priority, fire them all.</li>
     * </ul>
     * <li>For all states that will be exited, fire the exit state event.</li>
     * <li>For all transitions that fire, fire the transition action.</li>
     * <li>For all states that will be entered, fire the entry state event.</li>
     * </ol>
     * <li>... until no new transitions have fired.</li>
     * </ul>
     * <p/>
     * This method prevents itself from looping endlessly on a loop in the state
     * machine by only considering transitions that have not fired before in
     * this poll.
     */
    public void poll() {
        boolean stillNewTransitionsFiring;
        final var transitionsThatHaveFiredBefore = new HashSet<Transition<T, E, P>>();

        do {
            stillNewTransitionsFiring = false;
            final var statesToExit = new HashSet<T>();
            final var transitionsToFire = new HashSet<Transition<T, E, P>>();
            final var statesToEnter = new HashSet<T>();

            for (var sourceState : activeStates) {
                P firingPriority = null;
                for (var transition : findTransitionsForState(sourceState)) {
                    if (!transitionsThatHaveFiredBefore.contains(transition)) {
                        if (firingPriority != null && !transition.getPriority().equals(firingPriority)) {
                            // We reached a lower prio while higher prio transitions are firing.
                            // Don't consider these anymore, go to the next source state.
                            break;
                        }
                        if (transition.getCondition().isMet()) {
                            statesToExit.add(sourceState);
                            transitionsToFire.add(transition);
                            statesToEnter.add(transition.getDestinationState());
                            firingPriority = transition.getPriority();
                        }
                    }
                }
            }

            for (var stateToExit : statesToExit) {
                exitState(stateToExit);
            }
            for (var transitionToFire : transitionsToFire) {
                executeActions(transitionToFire.getActions());
                transitionsThatHaveFiredBefore.add(transitionToFire);
                stillNewTransitionsFiring = true;
            }
            for (var stateToEnter : statesToEnter) {
                enterState(stateToEnter);
            }

        } while (stillNewTransitionsFiring);
    }

    private void executeActions(Actions actions) {
        if (actions != null) {
            actions.execute();
        }
    }

    private void exitState(T state) {
        log.debug("exit state {}", state);
        if (activeStates.contains(state)) {
            executeExitActions(state);
            activeStates.remove(state);
        }
    }

    private void enterState(T newState) {
        if (endStates.contains(newState)) {
            log.debug("enter end state {}", newState);
            executeEntryActions(newState);
            if (activeStates.size() == 0) {
                log.debug("machine is finished");
            }
            return;
        }
        if (activeStates.add(newState)) {
            log.debug("enter state {}", newState);
            executeEntryActions(newState);
            resetTransitions(newState);
        }
    }

    private void resetTransitions(T sourceState) {
        for (var transition : transitions.get(sourceState)) {
            transition.getCondition().reset();
        }
    }

    private Queue<Transition<T, E, P>> findTransitionsForState(T sourceState) {
        return transitions.get(sourceState);
    }

    private void executeExitActions(T state) {
        executeActions(exitEvents.get(state));
    }

    private void executeEntryActions(T state) {
        executeActions(entryEvents.get(state));
    }

    /**
     * Gives access to the internals of the state machine.
     */
    public class Internals {
        /**
         * @return the end states.
         */
        public Set<T> getEndStates() {
            return new HashSet<>(StateMachine.this.endStates);
        }

        /**
         * @return the start states.
         */
        public Set<T> getStartStates() {
            return new HashSet<>(StateMachine.this.startStates);
        }

        /**
         * @return the states that have outgoing transitions defined.
         */
        public Set<T> getSourceStates() {
            return new HashSet<>(StateMachine.this.transitions.keySet());
        }

        /**
         * @return the outgoing transitions for a source state.
         */
        public Queue<Transition<T, E, P>> getTransitionsForSourceState( T sourceState) {
            return StateMachine.this.findTransitionsForState(sourceState);
        }

        // TODO complete meta information

        /**
         * Add 0 or more actions to be executed when the state is exited.
         */
        public void addExitActions(T state, Action... action) {
            log.debug("Create exit action for '{}' ({}) ", state, action);
            exitEvents.computeIfAbsent(state, t -> new Actions()).add(action);
        }

        /**
         * Add 0 or more actions to be executed when the state is entered.
         */
        public void addEntryActions(T state, Action... action) {
            log.debug("Create entry action for '{}' ({}) ", state, action);
            entryEvents.computeIfAbsent(state, t -> new Actions()).add(action);
        }

        /**
         * Add an end state.
         */
        public void addEndState(T endState) {
            log.debug("Add end state '{}'", endState);
            endStates.add(endState);
        }

        /**
         * Add a transition.
         */
        public void addTransition(Transition<T, E, P> transition) {
            final var sourceState = transition.getSourceState();
            log.debug("Create transition from '{}' to '{}' (pre: '{}', action: '{}')", sourceState,
                    transition.getDestinationState(), transition.getCondition(), transition.getActions());
            transitions.computeIfAbsent(sourceState, e -> new PriorityQueue<>()).add(transition);
        }

        /**
         * Adds a start state, and immediately activates it.
         */
        public void addStartState( T startState) {
            log.debug("Add start state '{}'", startState);
            startStates.add(startState);
            activeStates.add(startState);
        }

        /**
         * @return the statemachine whose internals these are.
         */
        public StateMachine<T, E, P> getStateMachine() {
            return StateMachine.this;
        }
    }
}
