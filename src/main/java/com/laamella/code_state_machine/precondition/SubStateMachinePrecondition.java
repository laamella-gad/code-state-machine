package com.laamella.code_state_machine.precondition;

import com.laamella.code_state_machine.StateMachine;

/**
 * A precondition that acts as a kind of sub-statemachine. The precondition is
 * met when the embedded statemachine has no active states left.
 * 
 * @param <E>
 *            event type. The same type as the parent state machine.
 */
// TODO test
public final class SubStateMachinePrecondition<T, E, P extends Comparable<P>> extends EventBasedPrecondition<E> {
	private final StateMachine<T, E, P> stateMachine;

	public SubStateMachinePrecondition(final StateMachine<T, E, P> stateMachine) {
		this.stateMachine = stateMachine;
	}

	@Override
	protected boolean preconditionIsMetAfterHandlingEvent(final E event) {
		stateMachine.handleEvent(event);
		return stateMachine.isFinished();
	}

	@Override
	public void reset() {
		super.reset();
		stateMachine.reset();
	}
}
