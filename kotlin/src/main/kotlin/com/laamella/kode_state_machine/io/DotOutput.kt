package com.laamella.kode_state_machine.io

import com.laamella.kode_state_machine.StateMachine

/**
 * Creates a simple "dot" diagram of the state machine. Start states are double
 * circles, end states are dotted circles, entry and exit events are not shown.
 */
fun <T, E, P : Comparable<P>> dotOutput(machine: StateMachine<T, E, P>): String {
    val internals = machine.Internals()

    val output = StringBuilder()
    output.append("digraph finite_state_machine {\n")
    output.append("\trankdir=LR;\n")
    output.append("\tsize=\"8,5\"\n")
    if (!internals.getStartStates().isEmpty()) {
        output.append("\tnode [shape = doublecircle, style=solid];")
        for (startState in internals.getStartStates()) {
            output.append(" ").append(startState)
        }
        output.append(";\n")
    }
    if (!internals.getEndStates().isEmpty()) {
        output.append("\tnode [shape = circle, style=dotted];")
        for (startState in internals.getEndStates()) {
            output.append(" ").append(startState)
        }
        output.append(";\n")
    }
    output.append("\tnode [shape = circle, style=solid];\n")
    for (sourceState in internals.getSourceStates()) {
        for (transition in internals.getTransitionsForSourceState(sourceState)!!) {
            output.append("\t").append(sourceState).append(" -> ").append(transition.destinationState)
            output.append(" [ label = \"").append(transition.conditions).append("\" ]")
            output.append(";\n")
        }
    }
    output.append("}\n")
    return output.toString()
}
