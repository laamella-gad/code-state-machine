package com.laamella.code_state_machine.condition;

import com.laamella.code_state_machine.Condition;

/**
 * A base class for conditions that do not respond to events.
 */
public abstract class NonEventBasedCondition<E> implements Condition<E> {
	@Override
	public final void handleEvent(final E event) {
		// Not event based, so not used.
	}

	@Override
	public void reset() {
		// Does nothing by default.
	}
}
