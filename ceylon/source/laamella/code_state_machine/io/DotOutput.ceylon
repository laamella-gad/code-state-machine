import laamella.code_state_machine {
	StateMachine
}

"Creates a simple 'dot' diagram of the state machine. Start states are double circles, end states are dotted circles, entry and exit events are not shown."
shared class DotOutput<State, Event, Priority>() 
	given Priority satisfies Comparable<Priority>
		given State satisfies Object
		given Event satisfies Object {
	
	shared String getOutput(StateMachine<State, Event, Priority> machine) {
		value internals = machine.Internals();
		
		value output = StringBuilder();
		output.append("digraph finite_state_machine {\n");
		output.append("\trankdir=LR;\n");
		output.append("\tsize=\"8,5\"\n");
		if (!internals.startStates.empty) {
			output.append("\tnode [shape = doublecircle, style=solid];");
			for (startState in internals.startStates) {
				output.append(" " + startState.string);
			}
			output.append(";\n");
		}
		if (!internals.endStates.empty) {
			output.append("\tnode [shape = circle, style=dotted];");
			for (startState in internals.endStates) {
				output.append(" " + startState.string);
			}
			output.append(";\n");
		}
		output.append("\tnode [shape = circle, style=solid];\n");
		for (sourceState in internals.sourceStates) {
			for (transition in internals.transitionsForSourceState(sourceState)) {
				output.append("\t``sourceState`` -> ``transition.destinationState``");
				output.append(" [ label = \"``transition.conditions``\" ]");
				output.append(";\n");
			}
		}
		output.append("}\n");
		return output.string;
	}
}
