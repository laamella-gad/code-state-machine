package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine.StateMachine

/**
 * Interface that all builder should adhere to.
 */
trait StateMachineBuilder[T, E, P <: Ordered[P]] {
	/** @return the passed machine, now filled with whatever the builder did */
	def build(newMachine: StateMachine[T, E, P]): StateMachine[T, E, P]

	/** @return a new machine */
	def build(): StateMachine[T, E, P]
}
