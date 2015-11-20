package com.laamella.code_state_machine

import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.priority.LeveledPriority._
import com.laamella.code_state_machine.util.{A, B, SimpleState}
import org.scalatest.BeforeAndAfterEach

class PriorityTests extends UnitSpec with BeforeAndAfterEach {
  private var trace: StringBuilder = _

  var builder: DslStateMachineBuilder[SimpleState, Object, Value] = _

  override def beforeEach() {
    builder = new DslStateMachineBuilder[SimpleState, Object, Value](NORMAL)
    builder.state(A).isAStartState
    builder.state(B).isAnEndState

    trace = new StringBuilder()
  }

  private def trace(signature: String): TraceAction = {
    new TraceAction(trace, signature)
  }

  behavior of "transitions with priorities"

  they should "only fire when they have the highest priority" in {
    builder.state(A).always.withPrio(HIGH).doing(trace("H")).goTo(B)
    builder.state(A).always.withPrio(NORMAL).doing(trace("N")).goTo(B)
    builder.state(A).always.withPrio(LOWEST).doing(trace("L")).goTo(B)
    val machine = builder.build()

    machine.poll()

    assert("H" == trace.toString)
  }

  they should "only fire when their condition is met, even though higher priority transactions exist" in {
    builder.state(A).never.withPrio(HIGH).doing(trace("H")).goTo(B)
    builder.state(A).never.withPrio(NORMAL).doing(trace("N")).goTo(B)
    builder.state(A).always.withPrio(NORMAL).doing(trace("N")).goTo(B)
    builder.state(A).always.withPrio(NORMAL).doing(trace("N")).goTo(B)
    val machine = builder.build()

    machine.poll()

    assert("NN" == trace.toString)
  }

  they should "fire together when they have the same priority" in {
    builder.state(A).always.doing(trace("H")).withPrio(HIGH).goTo(B)
    builder.state(A).always.doing(trace("H")).withPrio(HIGH).goTo(B)
    builder.state(A).always.doing(trace("L")).withPrio(LOWEST).goTo(B)
    val machine = builder.build()

    machine.poll()
    assert("HH" == trace.toString)
  }

}
