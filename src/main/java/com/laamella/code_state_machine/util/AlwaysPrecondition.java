package com.laamella.code_state_machine.util;

/**
 * This precondition is always met.
 */
public final class AlwaysPrecondition<E> extends NonEventBasedPrecondition<E> {
	@Override
	public boolean isMet() {
		return true;
	}

	@Override
	public String toString() {
		return "always";
	}

	@Override
	public void reset() {
	}
}