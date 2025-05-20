package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.builder.stateMachine
import com.laamella.kode_state_machine.io.dotOutput
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.util.SimpleState
import com.laamella.kode_state_machine.util.SimpleState.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AutomaticFiringTests {
    private var machine: StateMachine<SimpleState, Any, Priority>? = null

    @BeforeEach
    fun before() {
        machine = stateMachine<SimpleState, Any, Priority>(Priority.NORMAL) {
            state(A) {
                isAStartState()
                transitionsTo(B)
            }
            state(B) { transitionsTo(C) }
            state(C) { transitionsTo(D) }
            state(D) { transitionsTo(E) }
            state(E) { isAnEndState() }
        }.build()
        log.trace("\n" + dotOutput(machine!!))
    }

    @Test
    fun automaticFirings() {
        StateMachineAssert.assertActive(machine!!, A)
        machine!!.poll()
        StateMachineAssert.assertActive(machine!!)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AutomaticFiringTests::class.java)
    }
}
