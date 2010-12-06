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
public class DslStateMachineBuilder<T, E> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	private Set<T> sourceStates = new HashSet<T>();
	private Precondition<E> storedPrecondition;
	private Action storedAction;
	private final StateMachine.Builder<T, E> builder = new StateMachine.Builder<T, E>();

	public DslStateMachineBuilder() {
		resetStoredTransitionVariables();
	}

	public StateMachine<T, E> buildMachine() {
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public DslStateMachineBuilder<T, E> state(final T state) {
		return states(state);
	}

	public DslStateMachineBuilder<T, E> states(final T... states) {
		sourceStates = new HashSet<T>(Arrays.asList(states));
		return this;
	}

	public DslStateMachineBuilder<T, E> except(final T... states) {
		for (final T state : states) {
			sourceStates.remove(state);
		}
		return this;
	}

	public DslStateMachineBuilder<T, E> onExit(final Action action) {
		for (final T sourceState : sourceStates) {
			builder.setExitAction(sourceState, action);
		}
		return this;
	}

	public DslStateMachineBuilder<T, E> onEntry(final Action action) {
		for (final T sourceState : sourceStates) {
			log.debug("Create entry action for {} ({})", sourceState, action);
			builder.setEntryAction(sourceState, action);
		}
		return this;
	}

	public DslStateMachineBuilder<T, E> transition(final T destinationState, final Precondition<E> precondition,
			final Action action) {
		for (final T sourceState : sourceStates) {
			builder.addTransition(new BasicTransition<T, E>(sourceState, destinationState, precondition, action));
		}
		resetStoredTransitionVariables();
		return this;
	}

	private void resetStoredTransitionVariables() {
		storedAction = nothing();
		storedPrecondition = always();
	}

	public DslStateMachineBuilder<T, E> when(final Precondition<E> precondition) {
		storedPrecondition = precondition;
		return this;
	}

	public DslStateMachineBuilder<T, E> when(final E... events) {
		storedPrecondition = is(events);
		return this;
	}

	public DslStateMachineBuilder<T, E> action(final Action action) {
		storedAction = action;
		return this;
	}

	public DslStateMachineBuilder<T, E> then(final T destinationState) {
		return transition(destinationState, storedPrecondition, storedAction);
	}

	public Precondition<E> always() {
		return new AlwaysPrecondition<E>();
	}

	public Precondition<E> after(final long milliseconds) {
		return new AfterPrecondition<T, E>(milliseconds);
	}

	public Precondition<E> is(final E... events) {
		assert events != null;
		assert events.length != 0;

		if (events.length == 1) {
			final E singleEvent = events[0];
			return new SingleEventMatchPrecondition<E>(singleEvent);
		}

		return new MultiEventMatchPrecondition<E>(events);
	}

	public Action nothing() {
		return new NoAction();
	}

	public DslStateMachineBuilder<T, E> isEndState() {
		for (final T state : sourceStates) {
			builder.addEndState(state);
		}
		return this;
	}

	public DslStateMachineBuilder<T, E> isStartState() {
		for (final T state : sourceStates) {
			builder.addStartState(state);
		}
		return this;
	}

	public DslStateMachineBuilder<T, E> areEndStates() {
		return isEndState();
	}

	public DslStateMachineBuilder<T, E> areStartStates() {
		return isStartState();
	}
}
