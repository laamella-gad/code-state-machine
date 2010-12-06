package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Precondition;

public abstract class EventBasedPrecondition<E> implements Precondition<E> {
	private boolean isMet = false;

	@Override
	public final boolean isMet() {
		return isMet;
	}

	@Override
	public void reset() {
		isMet = false;
		// Does nothing by default.
	}

	@Override
	public final void handleEvent(final E event) {
		if (preconditionIsMetAfterHandlingEvent(event)) {
			isMet = true;
		}
	}

	protected abstract boolean preconditionIsMetAfterHandlingEvent(E event);
}
