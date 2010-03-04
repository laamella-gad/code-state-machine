/**
 * 
 */
package com.laamella.code_state_machine.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.laamella.code_state_machine.Precondition;

public final class MultiEventMatchPrecondition<T, E> implements Precondition<E> {
	private final Set<E> matchEvents;

	public MultiEventMatchPrecondition(final E[] events) {
		matchEvents = new HashSet<E>(Arrays.asList(events));
	}

	@Override
	public boolean isMet(final E event) {
		return matchEvents.contains(event);
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
	public void reset() {
	}
}