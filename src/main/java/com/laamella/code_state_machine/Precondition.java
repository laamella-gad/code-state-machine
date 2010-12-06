package com.laamella.code_state_machine;

/**
 * A way to define a precondition that is met or not.
 * 
 * @param <E>
 *            event type.
 */
public interface Precondition<E> {
	/**
	 * Handle an event.
	 * 
	 * @param event
	 *            the event that has occurred.
	 */
	void handleEvent(E event);

	/**
	 * @return whether the precondition is met.
	 */
	boolean isMet();

	/**
	 * This method is called every time the sourceState for this transition is
	 * entered. It can be used to implement stateful transitions, like
	 * transitions that fire after a certain amount of time.
	 */
	void reset();
}
