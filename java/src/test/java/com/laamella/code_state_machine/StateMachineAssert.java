package com.laamella.code_state_machine;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StateMachineAssert {
    @SafeVarargs
    public static <T extends Enum<?>, E, P extends Comparable<P>> void assertActive(final StateMachine<T, E, P> machine, final T... expectedStates) {
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
