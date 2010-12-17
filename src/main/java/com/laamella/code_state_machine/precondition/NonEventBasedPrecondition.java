package com.laamella.code_state_machine.precondition;

import com.laamella.code_state_machine.Precondition;

public abstract class NonEventBasedPrecondition<E> implements Precondition<E> {
	@Override
	public final void handleEvent(final E event) {
		// Not event based, so not used.
	}

	@Override
	public void reset() {
		// Does nothing by default.
	}
}
