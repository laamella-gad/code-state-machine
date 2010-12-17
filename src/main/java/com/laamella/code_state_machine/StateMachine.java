package com.laamella.code_state_machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple state machine.
 * <p/>
 * Features:
 * <ul>
 * <li>It allows multiple start states</li>
 * <li>It allows multiple active states</li>
 * <li>It allows multiple end states</li>
 * <li>Each state can have one entry and one exit state</li>
 * <li>Each transition can have one action</li>
 * <li>It does not do any kind of compilation</li>
 * <li>Its code is easy to understand</li>
 * <li>The state type can be anything</li>
 * <li>The event type can be anything</li>
 * <li>The priority type can be anything as long as it's Comparable</li>
 * </ul>
 * 
 * @param <T>
 *            State type. Each state should have a single instance of this type.
 *            An enum is a good fit.
 * @param <E>
 *            Event type. Events come into the state machine from the outside
 *            world, and are used to trigger state transitions.
 * @param <P>
 *            Priority type. Will be used to give priorities to transitions.
 *            Enums and Integers are useful here.
 */
public class StateMachine<T, E, P extends Comparable<P>> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	private final Set<T> startStates = new HashSet<T>();
	private final Set<T> endStates = new HashSet<T>();
	private final Set<T> activeStates = new HashSet<T>();
	private final Map<T, Actions> exitEvents = new HashMap<T, Actions>();
	private final Map<T, Actions> entryEvents = new HashMap<T, Actions>();
	private final Map<T, Queue<Transition<T, E, P>>> transitions = new HashMap<T, Queue<Transition<T, E, P>>>();

	/**
	 * This class can only be created through its builder.
	 */
	private StateMachine() {
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
		for (final T startState : startStates) {
			enterState(startState);
		}
	}

	/**
	 * @return an immutable set of all active states.
	 */
	public Set<T> getActiveStates() {
		return Collections.unmodifiableSet(activeStates);
	}

	/**
	 * @param state
	 * @return whether the state is currently active.
	 */
	public boolean isActive(final T state) {
		return activeStates.contains(state);
	}

	/**
	 * @return whether no states are active. Can be caused by all active states
	 *         having disappeared into end states, or by having no start states
	 *         at all.
	 */
	public boolean isFinished() {
		return activeStates.size() == 0;
	}

	/**
	 * Handle an event coming from the user application. After sending the event
	 * to all transitions that have an active source state, poll() will be
	 * called.
	 * 
	 * @param event
	 *            some event that has happened.
	 */
	public void handleEvent(final E event) {
		log.debug("handle event {}", event);

		for (final T sourceState : activeStates) {
			for (final Transition<T, E, P> transition : findTransitionsForState(sourceState)) {
				transition.getPrecondition().handleEvent(event);
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
		boolean stillNewTransitionsFiring = true;
		final Set<Transition<T, E, P>> transitionsThatHaveFiredBefore = new HashSet<Transition<T, E, P>>();

		do {
			stillNewTransitionsFiring = false;
			final Set<T> statesToExit = new HashSet<T>();
			final Set<Transition<T, E, P>> transitionsToFire = new HashSet<Transition<T, E, P>>();
			final Set<T> statesToEnter = new HashSet<T>();

			for (final T sourceState : activeStates) {
				P firingPriority = null;
				for (final Transition<T, E, P> transition : findTransitionsForState(sourceState)) {
					if (!transitionsThatHaveFiredBefore.contains(transition)) {
						if (firingPriority != null && !transition.getPriority().equals(firingPriority)) {
							// We reached a lower prio while higher prio transitions are firing.
							// Don't consider these anymore, go to the next source state.
							break;
						}
						if (transition.getPrecondition().isMet()) {
							statesToExit.add(sourceState);
							transitionsToFire.add(transition);
							statesToEnter.add(transition.getDestinationState());
							firingPriority = transition.getPriority();
						}
					}
				}
			}

			for (final T stateToExit : statesToExit) {
				exitState(stateToExit);
			}
			for (final Transition<T, E, P> transitionToFire : transitionsToFire) {
				executeActions(transitionToFire.getActions());
				transitionsThatHaveFiredBefore.add(transitionToFire);
				stillNewTransitionsFiring = true;
			}
			for (final T stateToEnter : statesToEnter) {
				enterState(stateToEnter);
			}

		} while (stillNewTransitionsFiring);
	}

	private void executeActions(final Actions actions) {
		if (actions != null) {
			actions.execute();
		}
	}

	private void exitState(final T state) {
		log.debug("exit state {}", state);
		if (activeStates.contains(state)) {
			executeExitActions(state);
			activeStates.remove(state);
		}
	}

	private void enterState(final T newState) {
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

	private void resetTransitions(final T sourceState) {
		for (final Transition<T, E, P> transition : transitions.get(sourceState)) {
			transition.getPrecondition().reset();
		}
	}

	private Queue<Transition<T, E, P>> findTransitionsForState(final T sourceState) {
		return transitions.get(sourceState);
	}

	private void executeExitActions(final T state) {
		executeActions(exitEvents.get(state));
	}

	private void executeEntryActions(final T state) {
		executeActions(entryEvents.get(state));
	}

	/**
	 * @return meta information about the internals of the state machine.
	 *         Calling this should not be necessary for normal operation.
	 */
	public MetaInformation getMetaInformation() {
		return this.new MetaInformation();
	}

	public class MetaInformation {
		public Set<T> getEndStates() {
			return new HashSet<T>(StateMachine.this.endStates);
		}

		public Set<T> getStartStates() {
			return new HashSet<T>(StateMachine.this.startStates);
		}

		public Set<T> getSourceStates() {
			return new HashSet<T>(StateMachine.this.transitions.keySet());
		}

		public Set<Transition<T, E, P>> getTransitionsForSourceState(final T sourceState) {
			// TODO looks like this can be made a lot simpler.
			final Set<Transition<T, E, P>> transitions = new HashSet<Transition<T, E, P>>();
			if (StateMachine.this.transitions.containsKey(sourceState)) {
				for (final Transition<T, E, P> transition : StateMachine.this.transitions.get(sourceState)) {
					transitions.add(transition);
				}
			}
			return transitions;
		}
		// TODO complete meta information
	}

	/**
	 * The basic builder of the state machine. Other builders can use this to
	 * deliver nicer syntax.
	 */
	public static class Builder<T, E, P extends Comparable<P>> {
		private final StateMachine<T, E, P> machine;

		public Builder() {
			log.debug("Start building new machine");
			machine = new StateMachine<T, E, P>();
		}

		public StateMachine<T, E, P>.MetaInformation getMetaInformation() {
			return machine.getMetaInformation();
		}

		public void addExitActions(final T state, final Action... action) {
			log.debug("Create exit action for '{}' ({}) ", state, action);
			if (!machine.exitEvents.containsKey(state)) {
				machine.exitEvents.put(state, new Actions(action));
				return;
			}
			machine.exitEvents.get(state).add(action);
		}

		public void addEntryActions(final T state, final Action... action) {
			log.debug("Create entry action for '{}' ({}) ", state, action);
			if (!machine.entryEvents.containsKey(state)) {
				machine.entryEvents.put(state, new Actions(action));
				return;
			}
			machine.entryEvents.get(state).add(action);
		}

		public void addEndState(final T endState) {
			log.debug("Add end state '{}'", endState);
			machine.endStates.add(endState);
		}

		public void addTransition(final Transition<T, E, P> transition) {
			final T sourceState = transition.getSourceState();
			log.debug("Create transition from '{}' to '{}' (pre: '{}', action: '{}')", new Object[] { sourceState,
					transition.getDestinationState(), transition.getPrecondition(), transition.getActions() });
			if (!machine.transitions.containsKey(sourceState)) {
				machine.transitions.put(sourceState, new PriorityQueue<Transition<T, E, P>>());
			}
			machine.transitions.get(sourceState).add(transition);
		}

		public StateMachine<T, E, P> build() {
			log.debug("Done building new machine");
			machine.reset();
			return machine;
		}

		public void addStartState(final T startState) {
			log.debug("Add start state '{}'", startState);
			machine.startStates.add(startState);
		}

	}
}
