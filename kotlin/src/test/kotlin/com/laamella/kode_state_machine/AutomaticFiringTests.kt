package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.builder.stateMachine
import com.laamella.kode_state_machine.io.dotOutput
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.util.SimpleState
import com.laamella.kode_state_machine.util.SimpleState.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class AutomaticFiringTests {
    private var machine = stateMachine<SimpleState, Any, Priority>(Priority.NORMAL) {
        state(A) {
            isAStartState()
            transitionsTo(B)
        }
        state(B) { transitionsTo(C) }
        state(C) { transitionsTo(D) }
        state(D) { transitionsTo(E) }
        state(E) { isAnEndState() }
    }.build()

    @BeforeEach
    fun before() {
        logger.trace { "\n" + dotOutput(machine!!) }
    }

    @Test
    fun automaticFirings() {
        StateMachineAssert.assertActive(machine!!, A)
        machine!!.poll()
        StateMachineAssert.assertActive(machine!!)
    }
}
