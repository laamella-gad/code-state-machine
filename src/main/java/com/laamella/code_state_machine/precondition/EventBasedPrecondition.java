package com.laamella.code_state_machine.precondition;

import com.laamella.code_state_machine.Precondition;

/**
 * A base class for preconditions that are met depending on some kind of event
 * handling.
 */
public abstract class EventBasedPrecondition<E> implements Precondition<E> {
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
		if (!isMet && preconditionIsMetAfterHandlingEvent(event)) {
			isMet = true;
		}
	}

	/**
	 * @param event
	 *            the event to handle.
	 * @return whether the precondition is met.
	 */
	protected abstract boolean preconditionIsMetAfterHandlingEvent(E event);
}
