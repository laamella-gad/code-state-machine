package com.laamella.code_state_machine

import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.priority.LeveledPriority._
import com.laamella.code_state_machine.util.{A, B, SimpleState}
import org.scalatest.BeforeAndAfterEach

class PriorityTests extends UnitSpec with BeforeAndAfterEach {
  private var trace: StringBuilder = _

  private var machine: StateMachine[SimpleState, Object, Value] = _

  override def beforeEach() {
    machine = new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() = {
        state(A).isAStartState()
        state(B).isAnEndState()
      }
    }.build()

    trace = new StringBuilder()
  }

  private def trace(signature: String): TraceAction = {
    new TraceAction(trace, signature)
  }

  behavior of "transitions with priorities"

  they should "only fire when they have the highest priority" in {
    new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() = {
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), NORMAL, trace("N"))
        state(A).whenConditions(always()).transition(B, always(), LOWEST, trace("L"))
      }
    }.build(machine)

    machine.poll()
    assert("H" == trace.toString)
  }

  they should "only fire when their condition is met, even though higher priority transactions exist" in {
    new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      @Override
      protected def executeBuildInstructions() {
        state(A).whenConditions(always()).transition(B, never(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, never(), NORMAL, trace("N"))
        state(A).whenConditions(always()).transition(B, always(), NORMAL, trace("N"))
        state(A).whenConditions(always()).transition(B, always(), NORMAL, trace("N"))
      }
    }.build(machine)

    machine.poll()
    assert("NN" == trace.toString)
  }

  they should "fire together when they have the same priority" in {
    new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() {
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), LOWEST, trace("L"))
      }
    }.build(machine)

    machine.poll()
    assert("HH" == trace.toString)
  }

}
