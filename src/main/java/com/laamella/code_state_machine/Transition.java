package com.laamella.code_state_machine;

/**
 * A conditional transition between two states.
 * 
 * @param <T>
 *            type of state.
 * @param <E>
 *            type of event.
 */
public interface Transition<T, E> {
	Precondition<E> getPrecondition();

	Action<E> getAction();

	T getDestinationState();

	T getSourceState();
}