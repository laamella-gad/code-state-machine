package com.laamella.code_state_machine;

import static com.laamella.code_state_machine.builder.DslStateMachineBuilder.always;
import static com.laamella.code_state_machine.builder.DslStateMachineBuilder.never;
import static com.laamella.code_state_machine.util.SimpleState.A;
import static com.laamella.code_state_machine.util.SimpleState.B;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.laamella.code_state_machine.builder.DslStateMachineBuilder;
import com.laamella.code_state_machine.priority.Priority;
import com.laamella.code_state_machine.util.SimpleState;

public class PriorityTests {
	private final StringBuffer trace = new StringBuffer();

	private DslStateMachineBuilder<SimpleState, Object, Priority> machineBuilder;

	@Before
	public void before() {
		machineBuilder = new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
			{
				state(A).isAStartState();
				state(B).isAnEndState();
			}
		};
	}

	private TraceAction trace(final String signature) {
		return new TraceAction(trace, signature);
	}

	@Test
	public void highPrioIsTheOnlyOneFiring() {
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.LOWEST, trace("L"));

		machineBuilder.buildMachine().poll();
		assertEquals("H", trace.toString());
	}

	@Test
	public void normalPriosAreTheOnlyOnesFiringBecauseOtherPrioDoesntMeetCondition() {
		machineBuilder.state(A).when(always()).transition(B, never(), Priority.HIGH, trace("H"));
		machineBuilder.state(A).when(always()).transition(B, never(), Priority.NORMAL, trace("N"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));

		machineBuilder.buildMachine().poll();
		assertEquals("NN", trace.toString());
	}

	@Test
	public void equalPriosFireTogether() {
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
		machineBuilder.state(A).when(always()).transition(B, always(), Priority.LOWEST, trace("L"));

		machineBuilder.buildMachine().poll();
		assertEquals("HH", trace.toString());
	}

}
