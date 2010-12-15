package com.laamella.code_state_machine.util;

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

public class AutomaticFiringTests {
	private static final Logger log = LoggerFactory.getLogger(AutomaticFiringTests.class);

	private StateMachine<SimpleState, Object, Priority> machine;

	@Before
	public void before() {
		machine = new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
			{
				state(A).isAStartState().when(always()).then(B);
				state(B).when(always()).then(C);
				state(C).when(always()).then(D);
				state(D).when(always()).then(E);
				state(E).isAnEndState();
			}
		}.buildMachine();
		log.trace("\n" + new DotOutput<SimpleState, Object, Priority>(machine.getMetaInformation()).getOutput());
	}

	@Test
	public void automaticFirings() {
		assertActive(machine, A);
		machine.poll();
		assertActive(machine);
	}

}
