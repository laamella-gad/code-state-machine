package com.laamella.code_state_machine.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.StateMachine;

/**
 * A pretty "DSL" builder for a state machine.
 */
public class DslStateMachineBuilder<T, E, P extends Comparable<P>> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	public class DefiningState {
		private Set<T> sourceStates = new HashSet<T>();

		public DefiningState(Set<T> sourceStates) {
			this.sourceStates = sourceStates;
		}

		public DefiningState except(final T... states) {
			for (final T state : states) {
				sourceStates.remove(state);
			}
			return this;
		}

		public DefiningState onExit(final Action action) {
			for (final T sourceState : sourceStates) {
				builder.setExitAction(sourceState, action);
			}
			return this;
		}

		public DefiningState onEntry(final Action action) {
			for (final T sourceState : sourceStates) {
				log.debug("Create entry action for {} ({})", sourceState, action);
				builder.setEntryAction(sourceState, action);
			}
			return this;
		}

		public DefiningState isAnEndState() {
			for (final T state : sourceStates) {
				builder.addEndState(state);
			}
			return this;
		}

		public DefiningState isAStartState() {
			for (final T state : sourceStates) {
				builder.addStartState(state);
			}
			return this;
		}

		public DefiningState areEndStates() {
			return isAnEndState();
		}

		public DefiningState areStartStates() {
			return isAStartState();
		}

		public DefiningTransition when(final Precondition<E> precondition) {
			return new DefiningTransition(sourceStates, precondition);
		}

		public DefiningTransition when(final E... events) {
			return new DefiningTransition(sourceStates, is(events));
		}

	}

	public class DefiningTransition {
		private Precondition<E> storedPrecondition;
		private Action action = nothing();
		private final Set<T> sourceStates;
		private P priority = defaultPriority;

		public DefiningTransition(Set<T> sourceStates, Precondition<E> precondition) {
			this.sourceStates = sourceStates;
			this.storedPrecondition = precondition;
		}

		public DefiningTransition action(final Action action) {
			this.action = action;
			return this;
		}

		public DefiningState then(final T destinationState) {
			return transition(destinationState, storedPrecondition, action, priority);
		}

		public DefiningState transition(final T destinationState, final Precondition<E> precondition,
				final Action action, P priority) {
			for (final T sourceState : sourceStates) {
				builder.addTransition(new BasicTransition<T, E, P>(sourceState, destinationState, precondition, action,
						priority));
			}
			return new DefiningState(sourceStates);
		}

		public DefiningTransition withPrio(P priority) {
			this.priority = priority;
			return this;
		}
	}

	private final StateMachine.Builder<T, E, P> builder = new StateMachine.Builder<T, E, P>();
	private final P defaultPriority;

	public DslStateMachineBuilder(P defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

	public StateMachine<T, E, P> buildMachine() {
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public DefiningState state(final T state) {
		return states(state);
	}

	public DefiningState states(final T... states) {
		return new DefiningState(new HashSet<T>(Arrays.asList(states)));
	}

	public static <E> Precondition<E> always() {
		return new AlwaysPrecondition<E>();
	}

	public static <E> Precondition<E> never() {
		return new NeverPrecondition<E>();
	}

	public static <E> Precondition<E> after(final long milliseconds) {
		return new AfterPrecondition<E>(milliseconds);
	}

	public static <E> Precondition<E> is(final E... events) {
		assert events != null;
		assert events.length != 0;

		if (events.length == 1) {
			final E singleEvent = events[0];
			return new SingleEventMatchPrecondition<E>(singleEvent);
		}

		return new MultiEventMatchPrecondition<E>(events);
	}

	public static Action nothing() {
		return new NoAction();
	}

}
