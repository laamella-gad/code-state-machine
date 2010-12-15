package com.laamella.code_state_machine;

/**
 * A conditional transition between two states.
 * 
 * @param <T>
 *            type of state.
 * @param <E>
 *            type of event.
 */
public interface Transition<T, E, P extends Comparable<P>> extends Comparable<Transition<T, E, P>> {
	Precondition<E> getPrecondition();

	// TODO support a list of actions
	Action getAction();

	T getDestinationState();

	T getSourceState();

	P getPriority();
}