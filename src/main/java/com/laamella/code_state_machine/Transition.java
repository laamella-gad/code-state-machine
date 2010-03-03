package com.laamella.code_state_machine;

/**
 * A state machine transition.
 * 
 * @param <T>
 * @param <E>
 */
public abstract class Transition<T extends Enum<?>, E> {
	private final T destinationState;
	private final T sourceState;

	public Transition(final T sourceState, final T destinationState) {
		this.destinationState = destinationState;
		this.sourceState = sourceState;
	}

	public abstract boolean isValidTransitionForEvent(E event);

	public final T getDestinationState() {
		return destinationState;
	}

	public final T getSourceState() {
		return sourceState;
	}

	public void execute(final E event) {
		// Override when wanted
	}
}
