package com.laamella.code_state_machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple wrapper around a list of actions.
 */
public class ActionChain {
	private final List<Action> actions;

	public ActionChain(final Action... actions) {
		this.actions = new ArrayList<Action>(Arrays.asList(actions));
	}

	public void execute() {
		for (final Action action : actions) {
			action.execute();
		}
	}

	public void add(final Action... additionalActions) {
		actions.addAll(Arrays.asList(additionalActions));
	}

	public void add(final ActionChain additionalActions) {
		actions.addAll(additionalActions.actions);
	}

	public void remove(final Action action) {
		actions.remove(action);
	}

}
