package com.laamella.code_state_machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Actions {
	private final List<Action> actions;

	public Actions(final Action... actions) {
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

	public void add(final Actions additionalActions) {
		actions.addAll(additionalActions.actions);
	}

}
