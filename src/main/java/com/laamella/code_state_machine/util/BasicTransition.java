package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.Transition;

/**
 * A basic implementation of a state machine transition.
 */
public class BasicTransition<T, E, P extends Comparable<P>> implements Transition<T, E, P> {
	private final T destinationState;
	private final T sourceState;
	private final Precondition<E> precondition;
	private final Action action;
	private final P priority;

	public BasicTransition(final T sourceState, final T destinationState, final Precondition<E> precondition,
			final Action action, P priority) {
		assert destinationState != null;
		assert sourceState != null;
		assert precondition != null;
		assert action != null;
		assert priority != null;

		this.destinationState = destinationState;
		this.sourceState = sourceState;
		this.precondition = precondition;
		this.action = action;
		this.priority = priority;
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

	@Override
	public int compareTo(Transition<T, E, P> o) {
		return priority.compareTo(o.getPriority());
	}

	@Override
	public P getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return String.format("Transition from %s to %s, precondition %s, action %s, priority %s", sourceState,
				destinationState, precondition, action, priority);
	}
}
