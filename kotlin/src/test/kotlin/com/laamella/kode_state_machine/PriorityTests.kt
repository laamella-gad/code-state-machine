package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.builder.StateMachineBuilder
import com.laamella.kode_state_machine.builder.stateMachine
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.priority.Priority.*
import com.laamella.kode_state_machine.util.SimpleState
import com.laamella.kode_state_machine.util.SimpleState.A
import com.laamella.kode_state_machine.util.SimpleState.B
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PriorityTests {
    private val trace = StringBuffer()

    private var machineBuilder: StateMachineBuilder<SimpleState, Any, Priority> = stateMachine(NORMAL) {
        state(A) { isAStartState() }
        state(B) { isAnEndState() }
    }

    private fun trace(signature: String): TraceAction {
        return TraceAction(trace, signature)
    }

    @Test
    fun highPrioIsTheOnlyOneFiring() {
        val machine = machineBuilder.more {
            state(A) { transitionsTo(B, priority = HIGH, action = trace("H")) }
            state(A) { transitionsTo(B, priority = NORMAL, action = trace("N")) }
            state(A) { transitionsTo(B, priority = LOWEST, action = trace("L")) }
        }.build()

        machine.poll()
        Assertions.assertEquals("H", trace.toString())
    }

    @Test
    fun normalPriosAreTheOnlyOnesFiringBecauseOtherPrioDoesntMeetCondition() {
        val machine = machineBuilder.more {
            state(A) { transitionsTo(B, condition = never(), priority = HIGH, action = trace("H")) }
            state(A) { transitionsTo(B, condition = never(), action = trace("N")) }
            state(A) { transitionsTo(B, action = trace("N")) }
            state(A) { transitionsTo(B, action = trace("N")) }
        }.build()

        machine.poll()
        Assertions.assertEquals("NN", trace.toString())
    }

    @Test
    fun equalPriosFireTogether() {
        val machine = stateMachine<SimpleState, Any, Priority>(NORMAL) {
            state(A) { transitionsTo(B, priority = HIGH, action = trace("H")) }
            state(A) { transitionsTo(B, priority = HIGH, action = trace("H")) }
            state(A) { transitionsTo(B, priority = LOWEST, action = trace("L")) }
        }.build()

        machine.poll()
        Assertions.assertEquals("HH", trace.toString())
    }
}
