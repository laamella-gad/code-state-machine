package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Action;

/**
 * This action does nothing.
 */
public final class NoAction<E> implements Action<E> {
	@Override
	public void execute(final E event) {
	}

	@Override
	public String toString() {
		return "nothing";
	}
}