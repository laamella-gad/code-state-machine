package com.laamella.code_state_machine.precondition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This precondition is met when the event is equal to one of the events passed
 * in the constructor.
 */
public final class MultiEventMatchPrecondition<E> extends EventBasedPrecondition<E> {
	private final Set<E> matchEvents;

	public MultiEventMatchPrecondition(final E[] events) {
		matchEvents = new HashSet<E>(Arrays.asList(events));
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("one of (");
		for (final E matchEvent : matchEvents) {
			str.append(matchEvent.toString() + " ");
		}
		return str.append(")").toString();
	}

	@Override
	protected boolean preconditionIsMetAfterHandlingEvent(final E event) {
		return matchEvents.contains(event);
	}
}