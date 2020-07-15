package com.laamella.code_state_machine.condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This condition is met when the event is equal to one of the events passed in
 * the constructor.
 */
public final class MultiEventMatchCondition<E> extends EventBasedCondition<E> {
	private final Set<E> matchEvents;

	@SafeVarargs
	public MultiEventMatchCondition(final E... events) {
		matchEvents = new HashSet<>(Arrays.asList(events));
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("one of (");
		for (final E matchEvent : matchEvents) {
			str.append(matchEvent.toString()).append(" ");
		}
		return str.append(")").toString();
	}

	@Override
	protected boolean conditionIsMetAfterHandlingEvent(final E event) {
		return matchEvents.contains(event);
	}
}