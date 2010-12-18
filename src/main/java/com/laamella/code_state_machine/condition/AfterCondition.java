package com.laamella.code_state_machine.condition;

/**
 * This condition is met after a certain amount of milliseconds.
 */
public final class AfterCondition<E> extends NonEventBasedCondition<E> {
	private final long milliseconds;
	private long minimalMeetTime;

	public AfterCondition(final long milliseconds) {
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