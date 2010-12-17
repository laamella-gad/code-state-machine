package com.laamella.code_state_machine.precondition;

public final class SingleEventMatchPrecondition<E> extends EventBasedPrecondition<E> {
	private final E singleEvent;

	public SingleEventMatchPrecondition(final E singleEvent) {
		this.singleEvent = singleEvent;
	}

	@Override
	public String toString() {
		return "is " + singleEvent;
	}

	@Override
	protected boolean preconditionIsMetAfterHandlingEvent(final E event) {
		return singleEvent.equals(event);
	}
}