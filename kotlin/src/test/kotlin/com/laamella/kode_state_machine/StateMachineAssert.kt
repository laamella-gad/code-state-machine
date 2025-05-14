package com.laamella.kode_state_machine

import org.junit.jupiter.api.Assertions
import java.util.*

object StateMachineAssert {
    fun <T : Enum<*>, E, P : Comparable<P>> assertActive(
        machine: StateMachine<T, E, P>,
        vararg expectedStates: T
    ) {
        for (expectedState in expectedStates) {
            if (!machine.isActive(expectedState)) {
                Assertions.fail<Any?>("Expected $expectedState to be active.")
            }
        }
        val expectedStatesSet = HashSet(Arrays.asList(*expectedStates))
        for (actualState in machine.activeStates) {
            if (!expectedStatesSet.contains(actualState)) {
                Assertions.fail<Any?>("$actualState was active, but not expected.")
            }
        }
    }
}
