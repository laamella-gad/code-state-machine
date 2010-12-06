package com.laamella.code_state_machine;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.util.DotOutput;
import com.laamella.code_state_machine.util.DslStateMachineBuilder;
import static com.laamella.code_state_machine.SimpleState.*;

public class Tests2 {
	private static final Logger log = LoggerFactory.getLogger(Tests2.class);

	private static class MachineBuilder extends DslStateMachineBuilder<SimpleState, Object> {
		public MachineBuilder() {
			state(A).isStartState().then(B);
			state(B).then(C);
			state(C).then(D);
			state(D).then(E);
			state(E).isEndState();
		}
	}

	private StateMachine<SimpleState, Object> machine;

	@Before
	public void before() {
		machine = new MachineBuilder().buildMachine();
		log.trace("\n" + new DotOutput<SimpleState, Object>(machine.getMetaInformation()).getOutput());
	}

	@Test
	public void automaticFirings() {
		assertActive(machine, A);
		machine.poll();
		assertActive(machine);
	}

	private static <T extends Enum<?>, E> void assertActive(final StateMachine<T, E> machine, final T... expectedStates) {
		for (final T expectedState : expectedStates) {
			if (!machine.isActive(expectedState)) {
				fail("Expected " + expectedState + " to be active.");
			}
		}
		final Set<T> expectedStatesSet = new HashSet<T>(Arrays.asList(expectedStates));
		for (final T actualState : machine.getActiveStates()) {
			if (!expectedStatesSet.contains(actualState)) {
				fail("" + actualState + " was active, but not expected.");
			}
		}
	}
}
