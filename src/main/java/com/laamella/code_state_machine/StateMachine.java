package com.laamella.code_state_machine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very basic state machine.
 * 
 * @param <T>
 *            enum of states
 * @param <E>
 *            event type
 */
public class StateMachine<T, E> {
	private static final Logger log = LoggerFactory
			.getLogger(StateMachine.class);

	private final Set<T> startStates = new HashSet<T>();
	private final Set<T> endStates = new HashSet<T>();
	private final Set<T> activeStates = new HashSet<T>();
	private final Map<T, Action<E>> exitEvents = new HashMap<T, Action<E>>();
	private final Map<T, Action<E>> entryEvents = new HashMap<T, Action<E>>();
	private final Map<T, Set<Transition<T, E>>> transitions = new HashMap<T, Set<Transition<T, E>>>();

	private StateMachine() {
		log.debug("New Machine");
	}

	public void reset() {
		log.debug("reset()");
		activeStates.clear();
		for (final T startState : startStates) {
			enterState(startState, null);
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

	public Set<T> getActiveStates() {
		return Collections.unmodifiableSet(activeStates);
	}

	public boolean isActive(final T state) {
		return activeStates.contains(state);
	}

	public void handleEvent(final E event) {
		final Set<T> statesToExit = new HashSet<T>();
		final Set<Transition<T, E>> transitionsToExecute = new HashSet<Transition<T, E>>();
		final Set<T> statesToEnter = new HashSet<T>();

		for (final T sourceState : activeStates) {
			for (final Transition<T, E> transition : findTransitionsForState(sourceState)) {
				if (transition.getPrecondition().isMet(event)) {
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

	public static class Builder<T, E> implements StateMachineBuilder<T, E> {

		private final StateMachine<T, E> machine;

		public Builder() {
			log.debug("Start building new machine");
			machine = new StateMachine<T, E>();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<T> getEndStates() {
			return new HashSet<T>(machine.endStates);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<T> getStartStates() {
			return new HashSet<T>(machine.startStates);
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<T> getSourceStates() {
			return new HashSet<T>(machine.transitions.keySet());
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<Transition<T, E>> getTransitionsForSourceState(
				final T sourceState) {
			final Set<Transition<T, E>> transitions = new HashSet<Transition<T, E>>();
			if (machine.transitions.containsKey(sourceState)) {
				for (final Transition<T, E> transition : machine.transitions
						.get(sourceState)) {
					transitions.add(transition);
				}
			}
			return transitions;

		}

		/**
		 * {@inheritDoc}
		 */
		public void setExitAction(final T state, final Action<E> action) {
			log.debug("Create exit action for {} ({}) ", state, action);
			machine.exitEvents.put(state, action);
		}

		/**
		 * {@inheritDoc}
		 */
		public void setEntryAction(final T state, final Action<E> action) {
			log.debug("Create entry action for {} ({}) ", state, action);
			machine.entryEvents.put(state, action);
		}

		/**
		 * {@inheritDoc}
		 */
		public void addEndState(final T endState) {
			log.debug("Add end state {}", endState);
			machine.endStates.add(endState);
		}

		/**
		 * {@inheritDoc}
		 */
		public void addTransition(final Transition<T, E> transition) {
			final T sourceState = transition.getSourceState();
			log.debug("Create transition from {} to {} (pre: {}, action: {})",
					new Object[] { sourceState,
							transition.getDestinationState(),
							transition.getPrecondition(),
							transition.getAction() });
			if (!machine.transitions.containsKey(sourceState)) {
				machine.transitions.put(sourceState,
						new HashSet<Transition<T, E>>());
			}
			machine.transitions.get(sourceState).add(transition);
		}

		/**
		 * {@inheritDoc}
		 */
		public StateMachine<T, E> build() {
			log.debug("Done building new machine");
			machine.reset();
			return machine;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addStartState(final T startState) {
			log.debug("Add start state {}", startState);
			machine.startStates.add(startState);
		}

	}
}
