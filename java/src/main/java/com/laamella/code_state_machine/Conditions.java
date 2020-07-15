package com.laamella.code_state_machine;

import com.laamella.code_state_machine.util.Chain;

/**
 * A simple wrapper around a list of conditions.
 */
public class Conditions<E> extends Chain<Condition<E>> {
	// This method exists only to suppress warnings about varargs.
	public Conditions() {
		super();
	}

	@SafeVarargs
	public Conditions(final Condition<E>... conditions) {
		super(conditions);
	}

	public void handleEvent(final E event) {
		for (final Condition<E> condition : getItems()) {
			condition.handleEvent(event);
		}
	}

	/**
	 * @return true if all conditions are met, else false.
	 */
	public boolean isMet() {
		for (final Condition<E> condition : getItems()) {
			if (!condition.isMet()) {
				return false;
			}
		}
		return true;
	}

	public void reset() {
		for (final Condition<E> condition : getItems()) {
			condition.reset();
		}
	}
}
