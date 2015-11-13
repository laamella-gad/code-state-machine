package com.laamella.code_state_machine;

import static com.laamella.code_state_machine.StateMachineAssert.assertActive;
import static com.laamella.code_state_machine.util.SimpleState.A;
import static com.laamella.code_state_machine.util.SimpleState.B;
import static com.laamella.code_state_machine.util.SimpleState.C;
import static com.laamella.code_state_machine.util.SimpleState.D;
import static com.laamella.code_state_machine.util.SimpleState.E;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.builder.DslStateMachineBuilder;
import com.laamella.code_state_machine.io.DotOutput;
import com.laamella.code_state_machine.priority.Priority;
import com.laamella.code_state_machine.util.SimpleState;

public class AutomaticFiringTests {
	private static final Logger log = LoggerFactory.getLogger(AutomaticFiringTests.class);

	private StateMachine<SimpleState, Object, Priority> machine;

	@Before
	public void before() {
		machine = new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
			@Override
			protected void executeBuildInstructions() {
				state(A).isAStartState().when(always()).then(B);
				state(B).when(always()).then(C);
				state(C).when(always()).then(D);
				state(D).when(always()).then(E);
				state(E).isAnEndState();
			}
		}.build(new StateMachine<SimpleState, Object, Priority>());
		log.trace("\n" + new DotOutput<SimpleState, Object, Priority>().getOutput(machine));
	}

	@Test
	public void automaticFirings() {
		assertActive(machine, A);
		machine.poll();
		assertActive(machine);
	}

}
