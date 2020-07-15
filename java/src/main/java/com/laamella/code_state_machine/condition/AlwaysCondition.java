package com.laamella.code_state_machine.condition;

/**
 * This condition is always met.
 */
public final class AlwaysCondition<E> extends NonEventBasedCondition<E> {
	@Override
	public boolean isMet() {
		return true;
	}

	@Override
	public String toString() {
		return "always";
	}
}