package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.StateMachineAssert.assertActive
import com.laamella.kode_state_machine.builder.DslStateMachineBuilder
import com.laamella.kode_state_machine.io.DotOutput
import com.laamella.kode_state_machine.priority.Priority
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StateTests {
    private var gameMachine: StateMachine<GameState, GameEvent, Priority>? = null

    @BeforeEach
    fun before() {
        val gameMachineBuilder: DslStateMachineBuilder<GameState, GameEvent, Priority>? =
            object : DslStateMachineBuilder<GameState, GameEvent, Priority>(
                Priority.NORMAL
            ) {
                override fun executeBuildInstructions() {
                    state(GameState.LOADER).onExit(log("exit!")).onEntry(log("enter!"))

                    state(GameState.LOADER).isAStartState.`when`(GameEvent.DONE).action(log("bing!"))
                        .then(GameState.INTRO)
                    state(GameState.INTRO).`when`(GameEvent.DONE).then(GameState.MENU)
                    state(GameState.MENU).`when`(GameEvent.START).then(GameState.GET_READY).`when`(GameEvent.ESCAPE)
                        .then(
                            GameState.EXIT
                        )
                    state(GameState.GET_READY).`when`(GameEvent.DONE).then(GameState.LEVEL)
                    state(GameState.LEVEL_FINISH).`when`(GameEvent.DONE).then(GameState.GET_READY)
                    state(GameState.LEVEL).`when`(GameEvent.DEAD).then(GameState.GAME_OVER).`when`(GameEvent.COMPLETE)
                        .then(
                            GameState.LEVEL_FINISH
                        )
                    state(GameState.GAME_OVER).`when`(GameEvent.DONE).then(GameState.MENU)
                    states(*GameState.entries.toTypedArray()).except(GameState.MENU, GameState.LOADER, GameState.EXIT)
                        .`when`(GameEvent.ESCAPE).then(
                            GameState.MENU
                        )

                    state(GameState.MENU).`when`(GameEvent.FIRE_A, GameEvent.FIRE_B).then(GameState.CONFIGURATION)
                    state(GameState.CONFIGURATION).`when`(GameEvent.FIRE_A, GameEvent.FIRE_B).then(GameState.MENU)

                    state(GameState.CONFIGURATION).`when`(GameEvent.FIRE_A).then(GameState.INTRO)

                    state(GameState.EXIT).isAnEndState
                }
            }
        gameMachine = gameMachineBuilder!!.build()
        log.trace("\n" + DotOutput().getOutput(gameMachine!!))
    }

    @Test
    fun concurrentStates() {
        gameMachine!!.handleEvent(GameEvent.DONE)
        gameMachine!!.handleEvent(GameEvent.DONE)
        gameMachine!!.handleEvent(GameEvent.FIRE_A)
        gameMachine!!.handleEvent(GameEvent.FIRE_A)
        assertActive(gameMachine!!, GameState.MENU, GameState.INTRO)
        gameMachine!!.handleEvent(GameEvent.START)
        assertActive(gameMachine!!, GameState.GET_READY, GameState.INTRO)
        gameMachine!!.handleEvent(GameEvent.DONE)
        assertActive(gameMachine!!, GameState.LEVEL, GameState.MENU)
        gameMachine!!.handleEvent(GameEvent.START)
        assertActive(gameMachine!!, GameState.LEVEL, GameState.GET_READY)
        gameMachine!!.handleEvent(GameEvent.DONE)
        assertActive(gameMachine!!, GameState.LEVEL)
    }

    @Test
    fun startStateIsLoader() {
        assertActive(gameMachine!!, GameState.LOADER)
    }

    @Test
    fun loadingDone() {
        gameMachine!!.handleEvent(GameEvent.DONE)
        assertActive(gameMachine!!, GameState.INTRO)
    }

    @Test
    fun endState() {
        assertActive(gameMachine!!, GameState.LOADER)
        gameMachine!!.handleEvent(GameEvent.DONE)
        assertActive(gameMachine!!, GameState.INTRO)
        gameMachine!!.handleEvent(GameEvent.DONE)
        assertActive(gameMachine!!, GameState.MENU)
        gameMachine!!.handleEvent(GameEvent.ESCAPE)
        assertActive(gameMachine!!)
    }

    @Test
    fun reset() {
        gameMachine!!.handleEvent(GameEvent.DONE)
        gameMachine!!.reset()
        assertActive(gameMachine!!, GameState.LOADER)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StateTests::class.java)
    }
}
