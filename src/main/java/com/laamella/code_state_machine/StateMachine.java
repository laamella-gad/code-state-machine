package com.laamella.code_state_machine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * <li>It's code is easy to understand</li>
 * </ul>
 * 
 * @param <T>
 *            State type. Each state should have a single instance of this type.
 *            An enum is a good fit.
 * @param <E>
 *            Event type. Events come into the state machine from the outside
 *            world, and are used to trigger state transitions.
 */
public class StateMachine<T, E> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	// TODO add global override transitions.
	private final Set<T> startStates = new HashSet<T>();
	private final Set<T> endStates = new HashSet<T>();
	private final Set<T> activeStates = new HashSet<T>();
	private final Map<T, Action<E>> exitEvents = new HashMap<T, Action<E>>();
	private final Map<T, Action<E>> entryEvents = new HashMap<T, Action<E>>();
	private final Map<T, Set<Transition<T, E>>> transitions = new HashMap<T, Set<Transition<T, E>>>();

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
		activeStates.clear();
		for (final T startState : startStates) {
			enterState(startState, null);
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
	 * Handle an event coming from the user application. The following happens:
	 * <ol>
	 * <li>For all applicable transitions, find the transitions that will fire
	 * for the supplied event.</li>
	 * <li>For all states that will be exited, fire the exit state event.</li>
	 * <li>For all transitions that fire, fire the transition action.</li>
	 * <li>For all states that will be entered, fire the entry state event.</li>
	 * </ol>
	 * <p>
	 * If multiple transitions can fire for a single source state, there is no
	 * selection step as is usually the case in state machines. The state
	 * machine will simply fire all of the transitions, creating multiple new
	 * states.
	 * </p>
	 * 
	 * @param event
	 *            the event that has occurred.
	 */
	public void handleEvent(final E event) {
		// FIXME fire all automatic transitions
		final Set<T> statesToExit = new HashSet<T>();
		final Set<Transition<T, E>> transitionsToExecute = new HashSet<Transition<T, E>>();
		final Set<T> statesToEnter = new HashSet<T>();

		for (final T sourceState : activeStates) {
			for (final Transition<T, E> transition : findTransitionsForState(sourceState)) {
				final Precondition<E> precondition = transition.getPrecondition();
				precondition.handleEvent(event);
				if (precondition.isMet()) {
					statesToExit.add(sourceState);
					transitionsToExecute.add(transition);
					statesToEnter.add(transition.getDestinationState());
				}
			}
		}
		for (final T stateToExit : statesToExit) {
			exitState(stateToExit, event);
		}
		for (final Transition<T, E> transitionToExecute : transitionsToExecute) {
			transitionToExecute.getAction().execute(event);
		}
		for (final T stateToEnter : statesToEnter) {
			enterState(stateToEnter, event);
		}
	}

	private void exitState(final T state, final E event) {
		log.debug("exit state {} ({})", state, event);
		if (activeStates.contains(state)) {
			executeExitAction(state, event);
			activeStates.remove(state);
		}
	}

	private void enterState(final T newState, final E event) {
		if (endStates.contains(newState)) {
			log.debug("enter end state {} ({})", newState, event);
			executeEntryAction(newState, event);
			if (activeStates.size() == 0) {
				log.debug("machine is finished");
			}
			return;
		}
		if (activeStates.add(newState)) {
			log.debug("enter state {} ({})", newState, event);
			executeEntryAction(newState, event);
			resetTransitions(newState);
		}
	}

	private void resetTransitions(final T sourceState) {
		for (final Transition<T, E> transition : transitions.get(sourceState)) {
			transition.getPrecondition().reset();
		}
	}

	private Set<Transition<T, E>> findTransitionsForState(final T sourceState) {
		return transitions.get(sourceState);
	}

	private void executeExitAction(final T state, final E event) {
		final Action<E> action = exitEvents.get(state);
		if (action != null) {
			action.execute(event);
		}
	}

	private void executeEntryAction(final T state, final E event) {
		final Action<E> action = entryEvents.get(state);
		if (action != null) {
			action.execute(event);
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

		public Set<Transition<T, E>> getTransitionsForSourceState(final T sourceState) {
			final Set<Transition<T, E>> transitions = new HashSet<Transition<T, E>>();
			if (StateMachine.this.transitions.containsKey(sourceState)) {
				for (final Transition<T, E> transition : StateMachine.this.transitions.get(sourceState)) {
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
	public static class Builder<T, E> {
		private final StateMachine<T, E> machine;

		public Builder() {
			log.debug("Start building new machine");
			machine = new StateMachine<T, E>();
		}

		public StateMachine<T, E>.MetaInformation getMetaInformation() {
			return machine.getMetaInformation();
		}

		public void setExitAction(final T state, final Action<E> action) {
			log.debug("Create exit action for {} ({}) ", state, action);
			machine.exitEvents.put(state, action);
		}

		public void setEntryAction(final T state, final Action<E> action) {
			log.debug("Create entry action for {} ({}) ", state, action);
			machine.entryEvents.put(state, action);
		}

		public void addEndState(final T endState) {
			log.debug("Add end state {}", endState);
			machine.endStates.add(endState);
		}

		public void addTransition(final Transition<T, E> transition) {
			final T sourceState = transition.getSourceState();
			log.debug("Create transition from {} to {} (pre: {}, action: {})",
					new Object[] { sourceState, transition.getDestinationState(), transition.getPrecondition(),
							transition.getAction() });
			if (!machine.transitions.containsKey(sourceState)) {
				machine.transitions.put(sourceState, new HashSet<Transition<T, E>>());
			}
			machine.transitions.get(sourceState).add(transition);
		}

		public StateMachine<T, E> build() {
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
