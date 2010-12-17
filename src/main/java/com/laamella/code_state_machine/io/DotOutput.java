package com.laamella.code_state_machine.io;

import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.Transition;

/**
 * Creates a "dot" diagram of the state machine.
 */
public class DotOutput<T, E, P extends Comparable<P>> {
	private final StateMachine<T, E, P>.MetaInformation metaInformation;

	public DotOutput(final StateMachine<T, E, P>.MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
	}

	public String getOutput() {
		final StringBuilder output = new StringBuilder();
		output.append("digraph finite_state_machine {\n");
		output.append("\trankdir=LR;\n");
		output.append("\tsize=\"8,5\"\n");
		output.append("\tnode [shape = doublecircle, style=solid];");
		for (final T startState : metaInformation.getStartStates()) {
			output.append(" " + startState);
		}
		output.append(";\n");
		output.append("\tnode [shape = circle, style=dotted];");
		for (final T startState : metaInformation.getEndStates()) {
			output.append(" " + startState);
		}
		output.append(";\n");
		output.append("\tnode [shape = circle, style=solid];\n");
		for (final T sourceState : metaInformation.getSourceStates()) {
			for (final Transition<T, E, P> transition : metaInformation.getTransitionsForSourceState(sourceState)) {
				output.append("\t" + sourceState + " -> " + transition.getDestinationState());

				output.append(" [ label = \"" + transition.getPrecondition() + "\" ]");

				output.append(";\n");
			}
		}
		output.append("}\n");
		return output.toString();
	}
}