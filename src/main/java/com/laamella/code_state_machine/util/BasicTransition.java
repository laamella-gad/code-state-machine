package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.Transition;

/**
 * A basic implementation of a state machine transition.
 * 
 * @param <T>
 *            the state type.
 * @param <E>
 *            the event type.
 */
public class BasicTransition<T, E> implements Transition<T, E> {
	private final T destinationState;
	private final T sourceState;
	private final Precondition<E> precondition;
	private final Action<E> action;

	public BasicTransition(final T sourceState, final T destinationState,
			final Precondition<E> precondition, final Action<E> action) {
		assert destinationState != null;
		assert sourceState != null;
		assert precondition != null;
		assert action != null;

		this.destinationState = destinationState;
		this.sourceState = sourceState;
		this.precondition = precondition;
		this.action = action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T getDestinationState() {
		return destinationState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T getSourceState() {
		return sourceState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Action<E> getAction() {
		return action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Precondition<E> getPrecondition() {
		return precondition;
	}
}
