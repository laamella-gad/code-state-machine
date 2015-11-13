package com.laamella.code_state_machine

import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.priority.LeveledPriority._
import com.laamella.code_state_machine.util.{A, B, SimpleState}
import org.junit.Assert._
import org.junit.{Before, Test}

class PriorityTests {
  private val trace = new StringBuffer()

  private var machine: StateMachine[SimpleState, Object, Value] = _

  @Before
  def before() = {
    machine = new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() = {
        state(A).isAStartState()
        state(B).isAnEndState()
      }
    }.build()
  }

  private def trace(signature: String): TraceAction = {
    new TraceAction(trace, signature)
  }

  @Test
  def highPrioIsTheOnlyOneFiring() = {
    new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() = {
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), NORMAL, trace("N"))
        state(A).whenConditions(always()).transition(B, always(), LOWEST, trace("L"))
      }
    }.build(machine)

    machine.poll()
    assertEquals("H", trace.toString)
  }

  @Test
  def normalPriosAreTheOnlyOnesFiringBecauseOtherPrioDoesntMeetCondition() = {
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
    assertEquals("NN", trace.toString)
  }

  @Test
  def equalPriosFireTogether() = {
    new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL) {
      override protected def executeBuildInstructions() {
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), HIGH, trace("H"))
        state(A).whenConditions(always()).transition(B, always(), LOWEST, trace("L"))
      }
    }.build(machine)

    machine.poll()
    assertEquals("HH", trace.toString)
  }

}
