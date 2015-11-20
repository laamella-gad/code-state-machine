package com.laamella.code_state_machine.io

import com.laamella.code_state_machine.StateMachine
import com.laamella.code_state_machine.Transition

/**
 * Creates a simple "dot" diagram of the state machine. Start states are double
 * circles, end states are dotted circles, entry and exit events are not shown.
 */
class DotOutput[State, Event, Priority <: Ordered[Priority]] {
	def getOutput(machine: StateMachine[State, Event, Priority] ): String = {

		val output = new StringBuilder()
		output.append("digraph finite_state_machine {\n")
		output.append("\trankdir=LR;\n")
		output.append("\tsize=\"8,5\"\n")
		if (machine.startStates.nonEmpty) {
			output.append("\tnode [shape = doublecircle, style=solid];")
			for (startState <- machine.startStates) {
				output.append(" " + startState)
			}
			output.append(";\n")
		}
		if (machine.endStates.nonEmpty) {
			output.append("\tnode [shape = circle, style=dotted];")
			for (startState <- machine.endStates) {
				output.append(" " + startState)
			}
			output.append(";\n")
		}
		output.append("\tnode [shape = circle, style=solid];\n")
		for (sourceState <- machine.transitions.keys) {
			for (transition <- machine.transitions(sourceState)) {
				output.append("\t" + sourceState + " -> " + transition.destinationState)
				output.append(" [ label = \"" + transition.conditions + "\" ]")
				output.append(";\n")
			}
		}
		output.append("}\n")
		output.toString()
	}
}
