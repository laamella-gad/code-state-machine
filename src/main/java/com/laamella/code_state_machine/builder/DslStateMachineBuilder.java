package com.laamella.code_state_machine.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.ActionChain;
import com.laamella.code_state_machine.Transition;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.action.LogAction;
import com.laamella.code_state_machine.precondition.AfterPrecondition;
import com.laamella.code_state_machine.precondition.AlwaysPrecondition;
import com.laamella.code_state_machine.precondition.MultiEventMatchPrecondition;
import com.laamella.code_state_machine.precondition.NeverPrecondition;
import com.laamella.code_state_machine.precondition.SingleEventMatchPrecondition;

/**
 * A pretty "DSL" builder for a state machine.
 */
public class DslStateMachineBuilder<T, E, P extends Comparable<P>> {
	private static final Logger log = LoggerFactory.getLogger(DslStateMachineBuilder.class);

	public class DefiningState {
		private Set<T> sourceStates = new HashSet<T>();

		public DefiningState(final Set<T> sourceStates) {
			this.sourceStates = sourceStates;
		}

		public DefiningState except(final T... states) {
			for (final T state : states) {
				sourceStates.remove(state);
			}
			return this;
		}

		public DefiningState onExit(final Action... action) {
			for (final T sourceState : sourceStates) {
				builder.addExitActions(sourceState, action);
			}
			return this;
		}

		public DefiningState onEntry(final Action... action) {
			for (final T sourceState : sourceStates) {
				log.debug("Create entry action for {} ({})", sourceState, action);
				builder.addEntryActions(sourceState, action);
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
		private final Precondition<E> storedPrecondition;
		private final ActionChain actions = new ActionChain();
		private final Set<T> sourceStates;
		private P priority = defaultPriority;

		public DefiningTransition(final Set<T> sourceStates, final Precondition<E> precondition) {
			this.sourceStates = sourceStates;
			this.storedPrecondition = precondition;
		}

		public DefiningTransition action(final Action action) {
			this.actions.add(action);
			return this;
		}

		public DefiningState then(final T destinationState) {
			return transition(destinationState, storedPrecondition, priority, actions);
		}

		public DefiningState transition(final T destinationState, final Precondition<E> precondition, final P priority,
				final ActionChain actions) {
			this.actions.add(actions);
			for (final T sourceState : sourceStates) {
				builder.addTransition(new Transition<T, E, P>(sourceState, destinationState, precondition,
						priority, this.actions));
			}
			return new DefiningState(sourceStates);
		}

		public DefiningState transition(final T destinationState, final Precondition<E> precondition, final P priority,
				final Action... actions) {
			return transition(destinationState, precondition, priority, new ActionChain(actions));
		}

		public DefiningTransition withPrio(final P priority) {
			this.priority = priority;
			return this;
		}
	}

	private final StateMachine.Builder<T, E, P> builder = new StateMachine.Builder<T, E, P>();
	private final P defaultPriority;

	public DslStateMachineBuilder(final P defaultPriority) {
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

	public static Action log(final String logText) {
		return new LogAction(logText);
	}

}
