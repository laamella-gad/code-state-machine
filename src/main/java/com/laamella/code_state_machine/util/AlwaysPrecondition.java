/**
 * 
 */
package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Precondition;

public final class AlwaysPrecondition<T, E> implements Precondition<E> {
	@Override
	public boolean isMet(final E event) {
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