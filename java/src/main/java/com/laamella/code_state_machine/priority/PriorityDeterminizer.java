package com.laamella.code_state_machine.priority;

/**
 * By using Integer as the priority type, and assigning priorities by using this
 * class, the state machine will become deterministic since transitions that
 * have been defined earlier always get priority over transitions that have been
 * defined later.
 */
// TODO this is a rather odd solution.
public final class PriorityDeterminizer {
	private static int nextPriority = Integer.MAX_VALUE;

	public static Integer nextPriority() {
		return nextPriority--;
	}
}
