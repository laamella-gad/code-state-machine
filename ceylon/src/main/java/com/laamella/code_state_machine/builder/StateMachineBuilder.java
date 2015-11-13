package com.laamella.code_state_machine.builder;

import com.laamella.code_state_machine.StateMachine;

/**
 * Interface that all builder should adhere to.
 */
public interface StateMachineBuilder<T, E, P extends Comparable<P>> {
	/**
	 * @return the passed machine, now filled with whatever the builder did.
	 */
	StateMachine<T, E, P> build(StateMachine<T, E, P> newMachine) throws Exception;

	/**
	 * @return a new machine.
	 */
	StateMachine<T, E, P> build() throws Exception;
}
