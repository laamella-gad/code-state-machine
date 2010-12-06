package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.Transition;

/**
 * A basic implementation of a state machine transition.
 */
public class BasicTransition<T, E> implements Transition<T, E> {
	private final T destinationState;
	private final T sourceState;
	private final Precondition<E> precondition;
	private final Action action;

	public BasicTransition(final T sourceState, final T destinationState, final Precondition<E> precondition,
			final Action action) {
		assert destinationState != null;
		assert sourceState != null;
		assert precondition != null;
		assert action != null;

		this.destinationState = destinationState;
		this.sourceState = sourceState;
		this.precondition = precondition;
		this.action = action;
	}

	@Override
	public final T getDestinationState() {
		return destinationState;
	}

	@Override
	public final T getSourceState() {
		return sourceState;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public Precondition<E> getPrecondition() {
		return precondition;
	}
}
