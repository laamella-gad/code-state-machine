package com.laamella.code_state_machine;

/**
 * Any kind of user defined code that is executed when a certain event is
 * received.
 * 
 * @param <E>
 *            event type.
 */
public interface Action<E> {
	/**
	 * 
	 * @param event
	 *            the event that triggered this action. Can be null when it is
	 *            the action on entry of a start state.
	 */
	void execute(E event);
}
