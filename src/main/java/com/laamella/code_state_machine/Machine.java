package com.laamella.code_state_machine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.util.Assert;

/**
 * Very basic state machine.
 * 
 * @param <T>
 *            enum of states
 * @param <E>
 *            event type
 */
public class Machine<T, E> {
	private static final Logger log = LoggerFactory.getLogger(Machine.class);
	private final T startState;
	private final Set<T> activeStates = new HashSet<T>();
	private final Map<T, Action<E>> exitEvents = new HashMap<T, Action<E>>();
	private final Map<T, Action<E>> entryEvents = new HashMap<T, Action<E>>();
	private final Map<T, Set<Transition<T, E>>> transitions = new HashMap<T, Set<Transition<T, E>>>();

	private Machine(final T startState) {
		this.startState = startState;
		log.debug("New Machine with start state {}", startState);
		reset();
	}

	public void reset() {
		log.debug("reset()");
		activeStates.clear();
		enterState(startState, null);
	}

	private void exitState(final T state, final E event) {
		log.debug("exit state {} ({})", state, event);
		if (activeStates.contains(state)) {
			executeExitAction(state, event);
			activeStates.remove(state);
		}
	}

	private void enterState(final T newState, final E event) {
		log.debug("enter state {} ({})", newState, event);
		if (activeStates.add(newState)) {
			executeEntryAction(newState, event);
		}
	}

	private Set<Transition<T, E>> findTransitionsForState(final T sourceState) {
		return transitions.get(sourceState);
	}

	public Set<T> getActiveStates() {
		return activeStates;
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
				if (transition.isPreconditionMet(event)) {
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
			transitionToExecute.executeAction(event);
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

	public static class Builder<T extends Enum<?>, E> {
		private static final Logger log = LoggerFactory
				.getLogger(Machine.class);

		private final Machine<T, E> machine;
		private Set<T> activeStates = new HashSet<T>();

		private Precondition<E> storedPrecondition;

		private Action<E> storedAction;

		public Builder(final T startState) {
			log.debug("Start building new machine");
			machine = new Machine<T, E>(startState);
			resetStoredTransitionVariables();
		}

		public Machine<T, E> buildMachine() {
			log.debug("Done building new machine");
			return machine;
		}

		@SuppressWarnings("unchecked")
		public Builder<T, E> state(final T state) {
			return states(state);
		}

		public Builder<T, E> states(final T... states) {
			activeStates = new HashSet<T>(Arrays.asList(states));
			return this;
		}

		public Builder<T, E> onExit(final Action<E> action) {
			for (final T activeState : activeStates) {
				log.debug("Create exit action for {} ({}) ", activeState,
						action);
				machine.exitEvents.put(activeState, action);
			}
			return this;
		}

		public Builder<T, E> onEntry(final Action<E> action) {
			for (final T activeState : activeStates) {
				log.debug("Create entry action for {} ({})", activeState,
						action);
				machine.entryEvents.put(activeState, action);
			}
			return this;
		}

		public Builder<T, E> transition(final T destinationState,
				final Precondition<E> precondition, final Action<E> action) {
			for (final T activeState : activeStates) {
				log.debug(
						"Create transition from {} to {} (pre: {} action: {})",
						new Object[] { activeState, destinationState,
								precondition, action });

				if (!machine.transitions.containsKey(activeState)) {
					machine.transitions.put(activeState,
							new HashSet<Transition<T, E>>());
				}
				machine.transitions.get(activeState).add(
						new Transition<T, E>(activeState, destinationState,
								precondition, action));
			}
			resetStoredTransitionVariables();
			return this;
		}

		private void resetStoredTransitionVariables() {
			storedAction = nothing();
			storedPrecondition = always();
		}

		public Builder<T, E> when(final Precondition<E> precondition) {
			storedPrecondition = precondition;
			return this;
		}

		public Builder<T, E> when(final E... events) {
			storedPrecondition = is(events);
			return this;
		}

		public Builder<T, E> action(final Action<E> action) {
			storedAction = action;
			return this;
		}

		public Builder<T, E> then(final T destinationState) {
			return transition(destinationState, storedPrecondition,
					storedAction);
		}

		public Precondition<E> always() {
			return new Precondition<E>() {
				@Override
				public boolean isMet(final E event) {
					return true;
				}

				@Override
				public String toString() {
					return "always";
				}
			};
		}

		protected Precondition<E> is(final E... events) {
			Assert.notNull(events);
			Assert.notEmpty(events);

			if (events.length == 1) {
				final E singleEvent = events[0];
				return new Precondition<E>() {
					@Override
					public boolean isMet(final E event) {
						return singleEvent.equals(event);
					}

					@Override
					public String toString() {
						return "is " + singleEvent;
					}
				};
			}

			return new Precondition<E>() {
				private final Set<E> matchEvents = new HashSet<E>(Arrays
						.asList(events));

				@Override
				public boolean isMet(final E event) {
					return matchEvents.contains(event);
				}

				@Override
				public String toString() {
					final StringBuilder str = new StringBuilder("one of (");
					for (final E matchEvent : matchEvents) {
						str.append(matchEvent.toString() + " ");
					}
					return str.append(")").toString();
				}
			};
		}

		protected Action<E> nothing() {
			return new Action<E>() {
				@Override
				public void execute(final E event) {
				}

				@Override
				public String toString() {
					return "nothing";
				}
			};
		}

	}
}
