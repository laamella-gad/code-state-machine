package com.laamella.code_state_machine.io;

import com.laamella.code_state_machine.StateMachine;

/**
 * Creates a simple "dot" diagram of the state machine. Start states are double
 * circles, end states are dotted circles, entry and exit events are not shown.
 */
public class DotOutput<T, E, P extends Comparable<P>> {
    public String getOutput(StateMachine<T, E, P> machine) {
        var internals = machine.new Internals();

        var output = new StringBuilder();
        output.append("digraph finite_state_machine {\n");
        output.append("\trankdir=LR;\n");
        output.append("\tsize=\"8,5\"\n");
        if (!internals.getStartStates().isEmpty()) {
            output.append("\tnode [shape = doublecircle, style=solid];");
            for (var startState : internals.getStartStates()) {
                output.append(" ").append(startState);
            }
            output.append(";\n");
        }
        if (!internals.getEndStates().isEmpty()) {
            output.append("\tnode [shape = circle, style=dotted];");
            for (var startState : internals.getEndStates()) {
                output.append(" ").append(startState);
            }
            output.append(";\n");
        }
        output.append("\tnode [shape = circle, style=solid];\n");
        for (var sourceState : internals.getSourceStates()) {
            for (var transition : internals.getTransitionsForSourceState(sourceState)) {
                output.append("\t").append(sourceState).append(" -> ").append(transition.getDestinationState());
                output.append(" [ label = \"").append(transition.getCondition()).append("\" ]");
                output.append(";\n");
            }
        }
        output.append("}\n");
        return output.toString();
    }
}
