import laamella.code_state_machine {
	StateMachine
}

"Interface that all builders should adhere to."
shared interface StateMachineBuilder<State, Event, Priority>
		given State satisfies Object
		given Event satisfies Object
		given Priority satisfies Comparable<Priority> {
	"Return the passed machine, now filled with whatever the builder did, or return a new machine if none passed."
	shared formal StateMachine<State,Event,Priority> build(StateMachine<State,Event,Priority>? newMachine);
}
