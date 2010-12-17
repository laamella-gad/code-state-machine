package com.laamella.code_state_machine.action;

import com.laamella.code_state_machine.Action;

/**
 * This action does nothing.
 */
public final class NoAction implements Action {
	public static Action INSTANCE = new NoAction();

	private NoAction() {
		// Use INSTANCE
	}

	@Override
	public void execute() {
	}

	@Override
	public String toString() {
		return "nothing";
	}
}