/**
 * 
 */
package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Precondition;

public final class AfterPrecondition<T, E> implements Precondition<E> {
	private final long milliseconds;
	private long minimalMeetTime;

	public AfterPrecondition(final long milliseconds) {
		this.milliseconds = milliseconds;
	}

	@Override
	public boolean isMet(final E event) {
		return System.currentTimeMillis() > minimalMeetTime;
	}

	@Override
	public void reset() {
		minimalMeetTime = System.currentTimeMillis() + milliseconds;
	}
}