package com.laamella.code_state_machine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 */
// TODO priorities
public class StateMachine<T, E, P extends Comparable<P>> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	private final Set<T> startStates = new HashSet<T>();
	private final Set<T> endStates = new HashSet<T>();
	private final Set<T> activeStates = new HashSet<T>();
	// TODO support a list of exit events
	private final Map<T, Action> exitEvents = new HashMap<T, Action>();
	// TODO support a list of entry events
	private final Map<T, Action> entryEvents = new HashMap<T, Action>();
	private final Map<T, Set<Transition<T, E, P>>> transitions = new HashMap<T, Set<Transition<T, E, P>>>();

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
	 * Repeat...
	 * <ol>
	 * <li>For all transitions that have an active source state, find the
	 * transitions that will fire for the supplied event. Ignore transitions
	 * that have already fired in this poll().</li>
	 * <li>For all states that will be exited, fire the exit state event.</li>
	 * <li>For all transitions that fire, fire the transition action.</li>
	 * <li>For all states that will be entered, fire the entry state event.</li>
	 * </ol>
	 * ... until no transitions have fired.
	 * <p>
	 * If multiple transitions can fire for a single source state, there is no
	 * selection step as is usually the case in state machines. The state
	 * machine will simply fire all of the transitions, creating multiple new
	 * states.
	 * </p>
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
							// Don't consider these anymore.
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
				transitionToFire.getAction().execute();
				transitionsThatHaveFiredBefore.add(transitionToFire);
				stillNewTransitionsFiring = true;
			}
			for (final T stateToEnter : statesToEnter) {
				enterState(stateToEnter);
			}

		} while (stillNewTransitionsFiring);
	}

	private void exitState(final T state) {
		log.debug("exit state {}", state);
		if (activeStates.contains(state)) {
			executeExitAction(state);
			activeStates.remove(state);
		}
	}

	private void enterState(final T newState) {
		if (endStates.contains(newState)) {
			log.debug("enter end state {}", newState);
			executeEntryAction(newState);
			if (activeStates.size() == 0) {
				log.debug("machine is finished");
			}
			return;
		}
		if (activeStates.add(newState)) {
			log.debug("enter state {}", newState);
			executeEntryAction(newState);
			resetTransitions(newState);
		}
	}

	private void resetTransitions(final T sourceState) {
		for (final Transition<T, E, P> transition : transitions.get(sourceState)) {
			transition.getPrecondition().reset();
		}
	}

	private Set<Transition<T, E, P>> findTransitionsForState(final T sourceState) {
		return transitions.get(sourceState);
	}

	private void executeExitAction(final T state) {
		final Action action = exitEvents.get(state);
		if (action != null) {
			action.execute();
		}
	}

	private void executeEntryAction(final T state) {
		final Action action = entryEvents.get(state);
		if (action != null) {
			action.execute();
		}
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
			// FIXME
			final Set<Transition<T, E, P>> transitions = new HashSet<Transition<T, E, P>>();
			if (StateMachine.this.transitions.containsKey(sourceState)) {
				for (final Transition<T, E, P> transition : StateMachine.this.transitions.get(sourceState)) {
					transitions.add(transition);
				}
			}
			return transitions;
		}
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

		public void setExitAction(final T state, final Action action) {
			log.debug("Create exit action for {} ({}) ", state, action);
			machine.exitEvents.put(state, action);
		}

		public void setEntryAction(final T state, final Action action) {
			log.debug("Create entry action for {} ({}) ", state, action);
			machine.entryEvents.put(state, action);
		}

		public void addEndState(final T endState) {
			log.debug("Add end state {}", endState);
			machine.endStates.add(endState);
		}

		public void addTransition(final Transition<T, E, P> transition) {
			final T sourceState = transition.getSourceState();
			log.debug("Create transition from {} to {} (pre: {}, action: {})",
					new Object[] { sourceState, transition.getDestinationState(), transition.getPrecondition(),
							transition.getAction() });
			if (!machine.transitions.containsKey(sourceState)) {
				machine.transitions.put(sourceState, new TreeSet<Transition<T, E, P>>());
			}
			machine.transitions.get(sourceState).add(transition);
		}

		public StateMachine<T, E, P> build() {
			log.debug("Done building new machine");
			machine.reset();
			return machine;
		}

		public void addStartState(final T startState) {
			log.debug("Add start state {}", startState);
			machine.startStates.add(startState);
		}

	}
}
