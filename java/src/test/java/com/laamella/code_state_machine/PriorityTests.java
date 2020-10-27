package com.laamella.code_state_machine;

import com.laamella.code_state_machine.builder.DslStateMachineBuilder;
import com.laamella.code_state_machine.priority.Priority;
import com.laamella.code_state_machine.util.SimpleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.laamella.code_state_machine.util.SimpleState.A;
import static com.laamella.code_state_machine.util.SimpleState.B;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityTests {
    private final StringBuffer trace = new StringBuffer();

    private StateMachine<SimpleState, Object, Priority> machine;

    @BeforeEach
    void before() {
        machine = new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
            @Override
            protected void executeBuildInstructions() {
                state(A).isAStartState();
                state(B).isAnEndState();
            }
        }.build();
    }

    private TraceAction trace(final String signature) {
        return new TraceAction(trace, signature);
    }

    @Test
    void highPrioIsTheOnlyOneFiring() {
        new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
            @Override
            protected void executeBuildInstructions() {
                state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
                state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));
                state(A).when(always()).transition(B, always(), Priority.LOWEST, trace("L"));
            }
        }.build(machine);

        machine.poll();
        assertEquals("H", trace.toString());
    }

    @Test
    void normalPriosAreTheOnlyOnesFiringBecauseOtherPrioDoesntMeetCondition() {
        new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
            @Override
            protected void executeBuildInstructions() {
                state(A).when(always()).transition(B, never(), Priority.HIGH, trace("H"));
                state(A).when(always()).transition(B, never(), Priority.NORMAL, trace("N"));
                state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));
                state(A).when(always()).transition(B, always(), Priority.NORMAL, trace("N"));
            }
        }.build(machine);

        machine.poll();
        assertEquals("NN", trace.toString());
    }

    @Test
    void equalPriosFireTogether() {
        new DslStateMachineBuilder<SimpleState, Object, Priority>(Priority.NORMAL) {
            @Override
            protected void executeBuildInstructions() {
                state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
                state(A).when(always()).transition(B, always(), Priority.HIGH, trace("H"));
                state(A).when(always()).transition(B, always(), Priority.LOWEST, trace("L"));
            }
        }.build(machine);

        machine.poll();
        assertEquals("HH", trace.toString());
    }

}
