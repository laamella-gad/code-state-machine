package com.laamella.kode_state_machine

import org.junit.jupiter.api.Assertions.fail

object StateMachineAssert {
    fun <T, E, P : Comparable<P>> assertActive(
        machine: StateMachine<T, E, P>,
        vararg expectedStates: T
    ) {
        // TODO use assertj
        for (expectedState in expectedStates) {
            if (!machine.isActive(expectedState)) {
                fail<String>("Expected $expectedState to be active.")
            }
        }
        val expectedStatesSet = setOf(*expectedStates)
        for (actualState in machine.activeStates) {
            if (!expectedStatesSet.contains(actualState)) {
                fail<String>("$actualState was active, but not expected.")
            }
        }
    }
}
