package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.builder.DslStateMachineBuilder
import com.laamella.kode_state_machine.io.DotOutput
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.util.SimpleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AutomaticFiringTests {
    private var machine: StateMachine<SimpleState, Any, Priority>? = null

    @BeforeEach
    fun before() {
        machine = object : DslStateMachineBuilder<SimpleState, Any, Priority>(Priority.NORMAL) {
            override fun executeBuildInstructions() {
                state(SimpleState.A).isAStartState.`when`(always()).then(SimpleState.B)
                state(SimpleState.B).`when`(always()).then(SimpleState.C)
                state(SimpleState.C).`when`(always()).then(SimpleState.D)
                state(SimpleState.D).`when`(always()).then(SimpleState.E)
                state(SimpleState.E).isAnEndState
            }
        }.build(StateMachine())
        log.trace("\n" + DotOutput().getOutput(machine!!))
    }

    @Test
    fun automaticFirings() {
        StateMachineAssert.assertActive(machine!!, SimpleState.A)
        machine!!.poll()
        StateMachineAssert.assertActive(machine!!)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AutomaticFiringTests::class.java)
    }
}
