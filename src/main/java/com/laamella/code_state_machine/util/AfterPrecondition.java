package com.laamella.code_state_machine.util;

/**
 * This precondition is met after a certain amount of milliseconds.
 */
public final class AfterPrecondition<T, E> extends NonEventBasedPrecondition<E> {
	private final long milliseconds;
	private long minimalMeetTime;

	public AfterPrecondition(final long milliseconds) {
		this.milliseconds = milliseconds;
	}

	@Override
	public boolean isMet() {
		return System.currentTimeMillis() > minimalMeetTime;
	}

	@Override
	public void reset() {
		minimalMeetTime = System.currentTimeMillis() + milliseconds;
	}
}