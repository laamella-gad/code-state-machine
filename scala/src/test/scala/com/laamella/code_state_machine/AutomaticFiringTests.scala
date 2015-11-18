package com.laamella.code_state_machine

import com.laamella.code_state_machine.StateMachineAssert._
import com.laamella.code_state_machine.builder.DslStateMachineBuilder
import com.laamella.code_state_machine.io.DotOutput
import com.laamella.code_state_machine.priority.LeveledPriority
import com.laamella.code_state_machine.priority.LeveledPriority.NORMAL
import com.laamella.code_state_machine.util._
import grizzled.slf4j.Logging
import org.junit.{Before, Test}

class AutomaticFiringTests extends Logging {
	private var machine: StateMachine[SimpleState, Object, LeveledPriority.Value]= _

	@Before def before() {
		machine = new DslStateMachineBuilder[SimpleState, Object, LeveledPriority.Value](NORMAL) {
			@Override def executeBuildInstructions() {
				state(A).isAStartState().whenConditions(always()).then(B)
				state(B).whenConditions(always()).then(C)
				state(C).whenConditions(always()).then(D)
				state(D).whenConditions(always()).then(E)
				state(E).isAnEndState()
			}
		}.build(new StateMachine[SimpleState, Object, LeveledPriority.Value]())
		trace("\n" + new DotOutput[SimpleState, Object, LeveledPriority.Value]().getOutput(machine))
	}

	@Test
	def automaticFirings() {
		assertActive(machine, A)
		machine.poll()
		assertActive(machine)
	}

}
