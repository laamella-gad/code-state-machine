package com.laamella.code_state_machine.precondition;


/**
 * This precondition is never met.
 */
public final class NeverPrecondition<E> extends NonEventBasedPrecondition<E> {
	@Override
	public boolean isMet() {
		return false;
	}

	@Override
	public String toString() {
		return "never";
	}

	@Override
	public void reset() {
	}
}