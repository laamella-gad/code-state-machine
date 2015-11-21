import ceylon.collection { MutableSet,
	HashSet }
"Interface that all builders should adhere to."
shared interface StateMachineBuilder<State, Event, Priority> 
		given State satisfies Object
		given Event satisfies Object
		given Priority satisfies Comparable<Priority>
		{
	"Return the passed machine, now filled with whatever the builder did, or return a new machine if none passed."
	shared formal StateMachine<State, Event, Priority> build(StateMachine<State, Event, Priority>? newMachine) ;
}

"A pretty \"DSL\" builder for a state machine."
shared abstract class DslStateMachineBuilder<State, Event, Priority>(variable StateMachine<State, Event, Priority> machine,	Priority defaultPriority) satisfies StateMachineBuilder<State, Event, Priority> 
		given State satisfies Object
		given Event satisfies Object
		given Priority satisfies Comparable<Priority>{
	value log = loggerFactory.getLogger(`DslStateMachineBuilder<State, Event, Priority>`);
	
	shared class DefiningState(MutableSet<State> sourceStates, StateMachine<State, Event, Priority>.Internals internals) {
		shared DefiningState except(State+ states) {
			for (state in states) {
				sourceStates.remove(state);
			}
			return this;
		}
		
		shared DefiningState onExit(Action+ action) {
			for (sourceState in sourceStates) {
				internals.addExitActions(sourceState, *action);
			}
			return this;
		}
		
		shared DefiningState onEntry(Action+ action) {
			for (sourceState in sourceStates) {
				log.debug("Create entry action for ``sourceState`` (``action``)");
				internals.addEntryActions(sourceState, *action);
			}
			return this;
		}
		
		shared DefiningState isAnEndState() {
			for (state in sourceStates) {
				internals.addEndState(state);
			}
			return this;
		}
		
		shared DefiningState isAStartState() {
			for (state in sourceStates) {
				internals.addStartState(state);
			}
			return this;
		}
		
		shared DefiningState areEndStates() => isAnEndState();
		
		shared DefiningState areStartStates() => isAStartState();
		
		shared DefiningTransition when(Condition<Event>+ condition) {
			return DefiningTransition(sourceStates, Conditions<Event>(*condition), internals);
		}
		
		shared DefiningTransition when_(Event+ events) {
			return DefiningTransition(sourceStates, _is(*events), internals);
		}
	}
	
	shared class DefiningTransition(MutableSet<State> sourceStates, Conditions<Event> conditions, StateMachine<State, Event, Priority>.Internals internals) {
		Actions actions = Actions();
		variable Priority priority = defaultPriority;
		
		shared DefiningTransition action(Action action) {
			actions.add(action);
			return this;
		}
		
		shared DefiningState then_(State destinationState) {
			return transition(destinationState, conditions, priority, actions);
		}
		
		shared DefiningState transition(State destinationState, Conditions<Event> storedConditions2,
		Priority priority, Actions actions) {
			this.actions.add(*actions);
			for (sourceState in sourceStates) {
				internals.addTransition(Transition<State, Event, Priority>(sourceState, destinationState, storedConditions2,
				priority, this.actions));
			}
			return DefiningState(sourceStates, internals);
		}
		
		shared DefiningState _transition(State destinationState, Condition<Event> condition, Priority priority, Action* actions) {
			return transition(destinationState, Conditions<Event>(condition), priority, Actions(*actions));
		}
		
		shared DefiningTransition withPrio(Priority priority) {
			this.priority = priority;
			return this;
		}
	}
	
	shared actual StateMachine<State, Event, Priority> build(StateMachine<State, Event, Priority>? newMachine) {
		if( exists newMachine) {
			machine=newMachine;
		} 
		executeBuildInstructions();
		return machine;
	}
	
	shared formal void executeBuildInstructions();
	
	shared DefiningState state(State state) {
		return states(state);
	}
	
	shared DefiningState states(State+ states) {
		return DefiningState(HashSet<State>{elements = states;}, machine.Internals());
	}
	
	shared Condition<Event> active(State+ statesThatMustBeActive) {
		return StatesActiveCondition<State, Event, Priority>(machine, *statesThatMustBeActive);
	}
	
	shared Condition<Event> inactive(State+ statesThatMustBeInactive) {
		return StatesInactiveCondition<State, Event, Priority>(machine, *statesThatMustBeInactive);
	}
	
}

shared Condition<Event> always<Event>() {
	return AlwaysCondition<Event>();
}

shared Condition<E> never<E>() {
	return NeverCondition<E>();
}

shared Condition<E> after<E>(Integer milliseconds) {
	return AfterCondition<E>(milliseconds);
}

shared Conditions<Event> _is<Event> (Event+ events) given Event satisfies Object {
	if (events.size == 1) {
		value singleEvent = events[0];
		return Conditions<Event>(SingleEventMatchCondition<Event>(singleEvent));
	}
	
	return Conditions<Event>(MultiEventMatchCondition<Event>(*events));
}

shared Action log(String logText) {
	return LogAction(logText);
}

