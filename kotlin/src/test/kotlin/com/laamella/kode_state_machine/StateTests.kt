package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.GameEvent.*
import com.laamella.kode_state_machine.GameState.*
import com.laamella.kode_state_machine.StateMachineAssert.assertActive
import com.laamella.kode_state_machine.builder.DslStateMachineBuilder
import com.laamella.kode_state_machine.io.dotOutput
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.priority.Priority.NORMAL
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StateTests {
    fun testMachine(): StateMachine<GameState, GameEvent, Priority> {
        val gameMachine = object : DslStateMachineBuilder<GameState, GameEvent, Priority>(NORMAL) {
            override fun executeBuildInstructions() {
                state(LOADER).onExit(log("exit!")).onEntry(log("enter!"))

                state(LOADER).isAStartState().`when`(DONE).action(log("bing!")).then(INTRO)
                state(INTRO).`when`(DONE).then(MENU)
                state(MENU).`when`(START).then(GET_READY).`when`(ESCAPE).then(EXIT)
                state(GET_READY).`when`(DONE).then(LEVEL)
                state(LEVEL_FINISH).`when`(DONE).then(GET_READY)
                state(LEVEL).`when`(DEAD).then(GAME_OVER).`when`(COMPLETE).then(LEVEL_FINISH)
                state(GAME_OVER).`when`(DONE).then(MENU)
                states(*GameState.entries.toTypedArray<GameState>()).except(MENU, LOADER, EXIT).`when`(ESCAPE)
                    .then(MENU)

                state(MENU).`when`(FIRE_A, FIRE_B).then(CONFIGURATION)
                state(CONFIGURATION).`when`(FIRE_A, FIRE_B).then(MENU)

                state(CONFIGURATION).`when`(FIRE_A).then(INTRO)

                state(EXIT).isAnEndState()
            }
        }.build()
        log.trace("\n" + dotOutput(gameMachine))
        return gameMachine
    }

    @Test
    fun concurrentStates() {
        val gameMachine = testMachine()
        gameMachine.handleEvent(DONE)
        gameMachine.handleEvent(DONE)
        gameMachine.handleEvent(FIRE_A)
        gameMachine.handleEvent(FIRE_A)
        assertActive(gameMachine, MENU, INTRO)
        gameMachine.handleEvent(START)
        assertActive(gameMachine, GET_READY, INTRO)
        gameMachine.handleEvent(DONE)
        assertActive(gameMachine, LEVEL, MENU)
        gameMachine.handleEvent(START)
        assertActive(gameMachine, LEVEL, GET_READY)
        gameMachine.handleEvent(DONE)
        assertActive(gameMachine, LEVEL)
    }

    @Test
    fun startStateIsLoader() {
        val gameMachine = testMachine()
        assertActive(gameMachine, LOADER)
    }

    @Test
    fun loadingDone() {
        val gameMachine = testMachine()
        gameMachine.handleEvent(DONE)
        assertActive(gameMachine, INTRO)
    }

    @Test
    fun endState() {
        val gameMachine = testMachine()
        assertActive(gameMachine, LOADER)
        gameMachine.handleEvent(DONE)
        assertActive(gameMachine, INTRO)
        gameMachine.handleEvent(DONE)
        assertActive(gameMachine, MENU)
        gameMachine.handleEvent(ESCAPE)
        assertActive(gameMachine)
    }

    @Test
    fun reset() {
        val gameMachine = testMachine()
        gameMachine.handleEvent(DONE)
        gameMachine.reset()
        assertActive(gameMachine, LOADER)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StateTests::class.java)
    }
}
