package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Action;

/**
 * This action does nothing.
 */
public final class NoAction implements Action {
	@Override
	public void execute() {
	}

	@Override
	public String toString() {
		return "nothing";
	}
}