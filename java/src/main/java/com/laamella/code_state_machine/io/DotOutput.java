package com.laamella.code_state_machine.io;

import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.Transition;

/**
 * Creates a simple "dot" diagram of the state machine. Start states are double
 * circles, end states are dotted circles, entry and exit events are not shown.
 */
public class DotOutput<T, E, P extends Comparable<P>> {
	public String getOutput(final StateMachine<T, E, P> machine) {
		final StateMachine<T, E, P>.Internals internals = machine.new Internals();

		final StringBuilder output = new StringBuilder();
		output.append("digraph finite_state_machine {\n");
		output.append("\trankdir=LR;\n");
		output.append("\tsize=\"8,5\"\n");
		if (!internals.getStartStates().isEmpty()) {
			output.append("\tnode [shape = doublecircle, style=solid];");
			for (final T startState : internals.getStartStates()) {
				output.append(" ").append(startState);
			}
			output.append(";\n");
		}
		if (!internals.getEndStates().isEmpty()) {
			output.append("\tnode [shape = circle, style=dotted];");
			for (final T startState : internals.getEndStates()) {
				output.append(" ").append(startState);
			}
			output.append(";\n");
		}
		output.append("\tnode [shape = circle, style=solid];\n");
		for (final T sourceState : internals.getSourceStates()) {
			for (final Transition<T, E, P> transition : internals.getTransitionsForSourceState(sourceState)) {
				output.append("\t").append(sourceState).append(" -> ").append(transition.getDestinationState());
				output.append(" [ label = \"").append(transition.getCondition()).append("\" ]");
				output.append(";\n");
			}
		}
		output.append("}\n");
		return output.toString();
	}
}
