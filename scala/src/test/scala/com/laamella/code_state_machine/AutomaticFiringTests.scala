package com.laamella.code_state_machine

import com.laamella.code_state_machine.StateMachineAssert._
import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.io.DotOutput
import com.laamella.code_state_machine.priority.LeveledPriority
import com.laamella.code_state_machine.priority.LeveledPriority.NORMAL
import com.laamella.code_state_machine.util._
import grizzled.slf4j.Logging
import org.scalatest.Assertions._

class AutomaticFiringTests extends UnitSpec with Logging {
  val builder = new DslStateMachineBuilder[SimpleState, Object, LeveledPriority.Value](NORMAL)
  builder.state(A).isAStartState.always.goTo(B)
  builder.state(B).always.goTo(C)
  builder.state(C).always.goTo(D)
  builder.state(D).always.goTo(E)
  builder.state(E).isAnEndState

  val machine = builder.build()

  trace("\n" + new DotOutput[SimpleState, Object, LeveledPriority.Value]().getOutput(machine))

  "an automatically firing transition" should "fire automatically" in {
    assertActive(machine, A)
    machine.poll()
    assertActive(machine)
  }

}
