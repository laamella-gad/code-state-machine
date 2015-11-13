package com.laamella.code_state_machine.condition;

import com.laamella.code_state_machine.Condition;

/**
 * A base class for conditions that are met depending on some kind of event
 * handling.
 */
public abstract class EventBasedCondition<E> implements Condition<E> {
	private boolean isMet = false;

	@Override
	public final boolean isMet() {
		return isMet;
	}

	@Override
	public void reset() {
		isMet = false;
	}

	@Override
	public final void handleEvent(final E event) {
		if (!isMet && conditionIsMetAfterHandlingEvent(event)) {
			isMet = true;
		}
	}

	/**
	 * @param event
	 *            the event to handle.
	 * @return whether the condition is met.
	 */
	protected abstract boolean conditionIsMetAfterHandlingEvent(E event);
}
