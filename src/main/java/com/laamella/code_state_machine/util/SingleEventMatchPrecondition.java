/**
 * 
 */
package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Precondition;

public final class SingleEventMatchPrecondition<T, E> implements
		Precondition<E> {
	private final E singleEvent;

	public SingleEventMatchPrecondition(final E singleEvent) {
		this.singleEvent = singleEvent;
	}

	@Override
	public boolean isMet(final E event) {
		return singleEvent.equals(event);
	}

	@Override
	public String toString() {
		return "is " + singleEvent;
	}

	@Override
	public void reset() {
	}
}