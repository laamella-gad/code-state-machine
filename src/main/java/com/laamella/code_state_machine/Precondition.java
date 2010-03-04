package com.laamella.code_state_machine;

/**
 * A way to define a precondition that is met or not, based on a received event.
 * 
 * @param <E>
 *            event type.
 */
public interface Precondition<E> {
	/**
	 * @param event
	 *            the event that has occurred.
	 * @return whether the precondition is met by the event.
	 */
	boolean isMet(E event);

	/**
	 * This method is called every time the sourceState for this transition is
	 * entered. It can be used to implement stateful transitions, like
	 * transitions that fire after a certain amount of time.
	 */
	void reset();
}
