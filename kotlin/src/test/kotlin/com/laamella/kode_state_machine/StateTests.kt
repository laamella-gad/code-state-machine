package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.GameEvent.*
import com.laamella.kode_state_machine.GameState.*
import com.laamella.kode_state_machine.StateMachineAssert.assertActive
import com.laamella.kode_state_machine.builder.stateMachine
import com.laamella.kode_state_machine.io.dotOutput
import com.laamella.kode_state_machine.priority.Priority
import com.laamella.kode_state_machine.priority.Priority.NORMAL
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class StateTests {
    fun testMachine(): StateMachine<GameState, GameEvent, Priority> {
        val gameMachine = stateMachine(NORMAL) {
            state(LOADER) {
                onExit(log("exit!"))
                onEntry(log("enter!"))
            }
            state(LOADER) {
                isAStartState()
                transition { to(INTRO); onEvent(DONE); action(log("bing!")) }
            }
            state(INTRO) { transitionsTo(MENU, condition = isEvent(DONE)) }
            state(MENU) {
                transitionsTo(GET_READY, condition = isEvent(START))
                transitionsTo(EXIT, condition = isEvent(ESCAPE))
            }
            state(GET_READY) { transitionsTo(LEVEL, condition = isEvent(DONE)) }
            state(LEVEL_FINISH) {
                transitionsTo(GET_READY, condition = isEvent(DONE))
            }
            state(LEVEL) {
                transitionsTo(GAME_OVER, condition = isEvent(DEAD))
                transitionsTo(LEVEL_FINISH, condition = isEvent(COMPLETE))
            }
            state(GAME_OVER) { transitionsTo(MENU, condition = isEvent(DONE)) }
            states(*GameState.entries.toTypedArray<GameState>()) {
                except(MENU, LOADER, EXIT)
                transitionsTo(MENU, condition = isEvent(ESCAPE))
            }

            state(MENU) { transitionsTo(CONFIGURATION, condition = isEventOneOf(FIRE_A, FIRE_B)) }

            state(CONFIGURATION) {
                transitionsTo(MENU, condition = isEventOneOf(FIRE_A, FIRE_B))
                transitionsTo(INTRO, condition = isEvent(FIRE_A))
            }
            state(EXIT) { isAnEndState() }
        }.build()
        logger.trace { "\n" + dotOutput(gameMachine) }
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
}
