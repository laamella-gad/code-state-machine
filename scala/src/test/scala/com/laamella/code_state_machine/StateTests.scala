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
  var gameMachine: StateMachine[GameState, GameEvent, LeveledPriority.Value] = _

  override def beforeEach() {
    val builder = new DslStateMachineBuilder[GameState, GameEvent, LeveledPriority.Value](NORMAL)
    builder.state(LOADER).onExitLog("exit!").onEntryLog("enter!")

    builder.state(LOADER).isAStartState().onEvents(DONE).log("bing!").goTo(INTRO)
    builder.state(INTRO).onEvents(DONE).goTo(MENU)
    builder.state(MENU).onEvents(START).goTo(GET_READY).onEvents(ESCAPE).goTo(EXIT)
    builder.state(GET_READY).onEvents(DONE).goTo(LEVEL)
    builder.state(LEVEL_FINISH).onEvents(DONE).goTo(GET_READY)
    builder.state(LEVEL).onEvents(DEAD).goTo(GAME_OVER).onEvents(COMPLETE).goTo(LEVEL_FINISH)
    builder.state(GAME_OVER).onEvents(DONE).goTo(MENU)
    builder.states(LOADER, INTRO, MENU, CONFIGURATION, GET_READY, LEVEL, LEVEL_FINISH, GAME_OVER, EXIT).except(MENU, LOADER, EXIT).onEvents(ESCAPE).goTo(MENU)

    builder.state(MENU).onEvents(FIRE_A, FIRE_B).goTo(CONFIGURATION)
    builder.state(CONFIGURATION).onEvents(FIRE_A, FIRE_B).goTo(MENU)

    builder.state(CONFIGURATION).onEvents(FIRE_A).goTo(INTRO)

    builder.state(EXIT).isAnEndState()

    gameMachine = builder.build()
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
    val result: Boolean = List(true, false, true).foldLeft(true)(_ && _)
    println(result)
  }
}
