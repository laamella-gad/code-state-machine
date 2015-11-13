package com.laamella.code_state_machine;

import com.laamella.code_state_machine.util.Chain;

/**
 * A simple wrapper around a list of actions.
 */
public class Actions extends Chain<Action> {
	public Actions(final Action... actions) {
		super(actions);
	}

	public void execute() {
		for (final Action action : getItems()) {
			action.execute();
		}
	}
}
