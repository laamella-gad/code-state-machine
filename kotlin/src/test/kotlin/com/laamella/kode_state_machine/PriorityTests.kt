package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.builder.DslStateMachineBuilder
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.util.SimpleState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PriorityTests {
    private val trace = StringBuffer()

    private var machine: StateMachine<SimpleState, Any, Priority>? = null

    @BeforeEach
    fun before() {
        machine = object : DslStateMachineBuilder<SimpleState, Any, Priority>(Priority.NORMAL) {
            override fun executeBuildInstructions() {
                state(SimpleState.A).isAStartState
                state(SimpleState.B).isAnEndState
            }
        }.build()
    }

    private fun trace(signature: String): TraceAction {
        return TraceAction(trace, signature)
    }

    @Test
    fun highPrioIsTheOnlyOneFiring() {
        object : DslStateMachineBuilder<SimpleState, Any, Priority>(Priority.NORMAL) {
            override fun executeBuildInstructions() {
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.HIGH, trace("H"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.NORMAL, trace("N"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.LOWEST, trace("L"))
            }
        }.build(machine!!)

        machine!!.poll()
        Assertions.assertEquals("H", trace.toString())
    }

    @Test
    fun normalPriosAreTheOnlyOnesFiringBecauseOtherPrioDoesntMeetCondition() {
        object : DslStateMachineBuilder<SimpleState, Any, Priority>(Priority.NORMAL) {
            override fun executeBuildInstructions() {
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, never(), Priority.HIGH, trace("H"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, never(), Priority.NORMAL, trace("N"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.NORMAL, trace("N"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.NORMAL, trace("N"))
            }
        }.build(machine!!)

        machine!!.poll()
        Assertions.assertEquals("NN", trace.toString())
    }

    @Test
    fun equalPriosFireTogether() {
        object : DslStateMachineBuilder<SimpleState, Any, Priority>(Priority.NORMAL) {
            override fun executeBuildInstructions() {
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.HIGH, trace("H"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.HIGH, trace("H"))
                state(SimpleState.A).`when`(always())
                    .transition(SimpleState.B, always(), Priority.LOWEST, trace("L"))
            }
        }.build(machine!!)

        machine!!.poll()
        Assertions.assertEquals("HH", trace.toString())
    }
}
