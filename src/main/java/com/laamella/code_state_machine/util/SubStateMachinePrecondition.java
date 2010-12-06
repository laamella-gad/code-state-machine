package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.StateMachine;

/**
 * A precondition that acts as a kind of sub-statemachine. The precondition is
 * met when the embedded statemachine has no active states left.
 * 
 * @param <E>
 *            event type. The same type as the parent state machine.
 */
// TODO implement
public class SubStateMachinePrecondition<T, E> extends EventBasedPrecondition<E> {
	private final StateMachine<T, E> stateMachine;

	public SubStateMachinePrecondition(final StateMachine<T, E> stateMachine) {
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
