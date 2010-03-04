package com.laamella.code_state_machine.util;

import com.laamella.code_state_machine.Transition;
import com.laamella.code_state_machine.StateMachine.Builder;

public class DotOutput<T, E> {
	private final Builder<T, E> builder;

	public DotOutput(final Builder<T, E> builder) {
		this.builder = builder;
	}

	public String getOutput() {
		final StringBuilder output = new StringBuilder();
		output.append("digraph finite_state_machine {\n");
		output.append("\trankdir=LR;\n");
		output.append("\tsize=\"8,5\"\n");
		output.append("\tnode [shape = doublecircle];");
		for (final T startState : builder.getStartStates()) {
			output.append(" " + startState);
		}
		output.append(";\n");
		output.append("\tnode [shape = circle];\n");
		for (final T sourceState : builder.getSourceStates()) {
			for (final Transition<T, E> transition : builder
					.getTransitionsForSourceState(sourceState)) {
				output.append("\t" + sourceState + " -> "
						+ transition.getDestinationState());

				output.append(" [ label = \"" + transition.getPrecondition()
						+ "\" ]");

				output.append(";\n");
			}
		}
		output.append("}\n");
		return output.toString();
	}
}
