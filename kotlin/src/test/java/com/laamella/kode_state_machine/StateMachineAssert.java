package com.laamella.kode_state_machine;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.fail;

public class StateMachineAssert {
    @SafeVarargs
    public static <T extends Enum<?>, E, P extends Comparable<P>> void assertActive( StateMachine<T, E, P> machine,  T... expectedStates) {
        for (var expectedState : expectedStates) {
            if (!machine.isActive(expectedState)) {
                fail("Expected " + expectedState + " to be active.");
            }
        }
        var expectedStatesSet = new HashSet<>(Arrays.asList(expectedStates));
        for (var actualState : machine.activeStates) {
            if (!expectedStatesSet.contains(actualState)) {
                fail("" + actualState + " was active, but not expected.");
            }
        }
    }

}
