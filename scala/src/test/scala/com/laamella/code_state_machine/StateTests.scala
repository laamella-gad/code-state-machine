package com.laamella.code_state_machine

import com.laamella.code_state_machine.StateMachineAssert._
import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.io.DotOutput
import com.laamella.code_state_machine.priority.LeveledPriority
import com.laamella.code_state_machine.priority.LeveledPriority.NORMAL
import grizzled.slf4j.Logging
import org.junit.{Before, Test}
import org.scalatest.Assertions._
import org.scalatest.BeforeAndAfterEach

class StateTests extends UnitSpec with BeforeAndAfterEach with Logging {
  private var gameMachine: StateMachine[GameState, GameEvent, LeveledPriority.Value] = _

  override def beforeEach() {
    val gameMachineBuilder = new DslStateMachineBuilder[GameState, GameEvent, LeveledPriority.Value](NORMAL) {
      override def executeBuildInstructions() {
        state(LOADER).onExit(log("exit!")).onEntry(log("enter!"))

        state(LOADER).isAStartState().whenEvents(DONE).action(log("bing!")).then(INTRO)
        state(INTRO).whenEvents(DONE).then(MENU)
        state(MENU).whenEvents(START).then(GET_READY).whenEvents(ESCAPE).then(EXIT)
        state(GET_READY).whenEvents(DONE).then(LEVEL)
        state(LEVEL_FINISH).whenEvents(DONE).then(GET_READY)
        state(LEVEL).whenEvents(DEAD).then(GAME_OVER).whenEvents(COMPLETE).then(LEVEL_FINISH)
        state(GAME_OVER).whenEvents(DONE).then(MENU)
        states(LOADER, INTRO, MENU, CONFIGURATION, GET_READY, LEVEL, LEVEL_FINISH, GAME_OVER, EXIT).except(MENU, LOADER, EXIT).whenEvents(ESCAPE).then(MENU)

        state(MENU).whenEvents(FIRE_A, FIRE_B).then(CONFIGURATION)
        state(CONFIGURATION).whenEvents(FIRE_A, FIRE_B).then(MENU)

        state(CONFIGURATION).whenEvents(FIRE_A).then(INTRO)

        state(EXIT).isAnEndState()
      }
    }
    gameMachine = gameMachineBuilder.build()
    trace("\n" + new DotOutput[GameState, GameEvent, LeveledPriority.Value]().getOutput(gameMachine))
  }

  behavior of "a state machine"

  it should "support concurrent states" in {
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

  it should "have LOADER as the start state" in {
    assertActive(gameMachine, LOADER)
  }

  it should "loading is done, the intro starts" in {
    gameMachine.handleEvent(DONE)
    assertActive(gameMachine, INTRO)
  }

  it should "have no states left when we get to the end state" in {
    assertActive(gameMachine, LOADER)
    gameMachine.handleEvent(DONE)
    assertActive(gameMachine, INTRO)
    gameMachine.handleEvent(DONE)
    assertActive(gameMachine, MENU)
    gameMachine.handleEvent(ESCAPE)
    assertActive(gameMachine)
  }

  it should "reset to the start state correctly" in {
    gameMachine.handleEvent(DONE)
    gameMachine.reset()
    assertActive(gameMachine, LOADER)
  }

  it should "aaa" in {
    val result: Boolean = List(true, false, true).foldLeft(true) (_ && _)
    println(result)
  }
}
