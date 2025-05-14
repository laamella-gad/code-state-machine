package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.StateMachine

/**
 * Interface that all builder should adhere to.
 */
interface StateMachineBuilder<T, E, P : Comparable<P>> {
    /**
     * @return the passed machine, now filled with whatever the builder did.
     */
    fun build(newMachine: StateMachine<T, E, P>): StateMachine<T, E, P>

    /**
     * @return a new machine.
     */
    fun build(): StateMachine<T, E, P>
}
