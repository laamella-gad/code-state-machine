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
	 * User code.
	 */
	void execute();
}
