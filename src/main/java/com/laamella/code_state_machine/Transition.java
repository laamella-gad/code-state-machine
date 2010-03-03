package com.laamella.code_state_machine;

import com.laamella.code_state_machine.util.Assert;

/**
 * A state machine transition.
 * 
 * @param <T>
 * @param <E>
 */
public class Transition<T, E> {
	private final T destinationState;
	private final T sourceState;
	private final Precondition<E> precondition;
	private final Action<E> action;

	public Transition(final T sourceState, final T destinationState,
			final Precondition<E> precondition, final Action<E> action) {
		Assert.notNull(destinationState);
		Assert.notNull(sourceState);
		Assert.notNull(precondition);
		Assert.notNull(action);
		this.destinationState = destinationState;
		this.sourceState = sourceState;
		this.precondition = precondition;
		this.action = action;
	}

	public boolean isPreconditionMet(final E event) {
		return precondition.isMet(event);
	}

	public void executeAction(final E event) {
		action.execute(event);
	}

	public final T getDestinationState() {
		return destinationState;
	}

	public final T getSourceState() {
		return sourceState;
	}
}
