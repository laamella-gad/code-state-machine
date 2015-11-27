import ceylon.collection {
	HashSet,
	HashMap,
	MutableSet,
	PriorityQueue
}
import ceylon.logging {
	logger
}

"Any kind of user defined code that is executed when a certain event is received."
// TODO this should be a function.
shared interface Action {
	"User code."
	shared formal void execute();
}

"A simple wrapper around a list of actions."
shared class Actions(Action* values) extends Chain<Action>(*values) {
	shared void execute() {
		for (action in this) {
			action.execute();
		}
	}
}

shared Actions noActions = Actions();

"A way to define a condition that is met or not."
shared interface Condition<Event> {
	"Handle an event."
	shared formal void handleEvent(Event event);
	
	"Return whether the condition is met."
	shared formal Boolean met;
	
	"This method is called every time the sourceState for this transition is
	          entered. It can be used to implement stateful transitions, like
	          transitions that fire after a certain amount of time."
	shared formal void reset();
}

"A simple wrapper around a list of conditions."
shared class Conditions<Event>(Condition<Event>* conditions) extends Chain<Condition<Event>>(*conditions) {
	shared void handleEvent(Event event) {
		for (condition in this) {
			condition.handleEvent(event);
		}
	}
	
	"Return true if all conditions are met, else false."
	shared Boolean met {
		for (condition in this) {
			if (!condition.met) {
				return false;
			}
		}
		return true;
	}
	
	shared void reset() {
		for (condition in this) {
			condition.reset();
		}
	}
}

"A conditional transition between two states."
shared class Transition<State, Event, Priority>(
	shared State sourceState,
	shared State destinationState,
	shared Conditions<Event> conditions,
	shared Priority priority,
	shared Actions actions)
		satisfies Comparable<Transition<State,Event,Priority>>
		given Priority satisfies Comparable<Priority>
		given State satisfies Object
		given Event satisfies Object {
	"Compares transitions on their priorities."
	shared actual Comparison compare(Transition<State,Event,Priority> other) => priority <=> other.priority;
	
	shared actual String string = "Transition from ``sourceState`` to ``destinationState``, condition ``conditions``, action ``actions``, priority ``priority``";
}

"A programmer friendly state machine.

 Features:

 * It is non-deterministic, but has the tools to become deterministic.
 * It allows multiple start states.
 * It allows multiple active states.
 * It allows multiple end states.
 * States and their transitions do not have to form a single graph. Separate
 graphs may exist inside a single state machine.
 * Each state has a chain of entry and exit actions.
 * Each transition has a chain of actions.
 * It does not do any kind of compilation.
 * Its code is written in a straightforward way, and is hopefully easy to
 understand.
 * It has a priority system for transitions.
 * It does not have sub state machines; a state machine is not a state.
 * It has transitions that use a state machine for their condition.
 * With the DSL, transitions to a certain state can be added for multiple
 source states, thereby faking global transitions.
 * It tries to put as few constraints as possible on the user.
 * It has only one dependency: slf4j for logging, which can be configured to
 use any other logging framework.
 * The state type can be anything.
 * The event type can be anything.
 * The priority type can be anything as long as it's Comparable.
 * It has two, always accessible modes of usage: asking the state machine
 for the current state, or having the state machine trigger actions that
 change the user code state.
 
 Generic types:
 * State: each state should have a single instance of this type.
   An enum is a good fit.
 * Event: events come into the state machine from the outside
   world, and are used to trigger state transitions.
 * Priority: will be used to give priorities to transitions.
   Enums and Integers are useful here.
 
 To create the state machine, use the internals, or use one of the builders.
"
shared class StateMachine<State, Event, Priority>()
		given Priority satisfies Comparable<Priority>
		given State satisfies Object
		given Event satisfies Object {
	value log = logger(`package laamella.code_state_machine`);
	
	value startStates = HashSet<State>();
	value endStates = HashSet<State>();
	// TODO find an easy way to show only Set to outside
	shared MutableSet<State> activeStates = HashSet<State>();
	value exitEvents = HashMap<State,Actions>();
	value entryEvents = HashMap<State,Actions>();
	value transitions = HashMap<State,PriorityQueue<Transition<State,Event,Priority>>>();
	
	log.debug("New Machine");
	
	"Resets all active states to the start states."
	shared void reset() {
		log.debug("reset()");
		if (startStates.empty) {
			log.warn("State machine does not contain any start states.");
		}
		activeStates.clear();
		for (startState in startStates) {
			enterState(startState);
		}
	}
	
	"Returns a set of all active states."
	shared Set<State> activeStates_ => activeStates;
	
	"Returns whether the state is currently active."
	shared Boolean active(State state) => activeStates.contains(state);
	
	"Returns whether no states are active. Can be caused by all active states
	          having disappeared into end states, or by having no start states
	          at all."
	shared Boolean finished => activeStates.empty;
	
	"Handle an event coming from the user application. After sending the event
	          to all transitions that have an active source state, poll() will be
	          called."
	shared void handleEvent(Event event) {
		log.debug("handle event ``event``");
		
		for (sourceState in activeStates) {
			for (transition in findTransitionsForState(sourceState)) {
				transition.conditions.handleEvent(event);
			}
		}
		poll();
	}
	
	"
	          Tells the state machine to look for state changes to execute. This method
	          has to be called regularly, or the state machine will do nothing at all.
	      
	          * Repeat...
	              1. For all transitions that have an active source state, find the
	          		transitions that will fire.
	                  * Ignore transitions that have already fired in this poll().
	                  * For a single source state, find the transition of the highest
	          			priority which will fire (if any fire at all.) If multiple transitions
	          			share this priority, fire them all.
	              2. For all states that will be exited, fire the exit state event.
	              3. For all transitions that fire, fire the transition action.
	              4. For all states that will be entered, fire the entry state event.
	          * ... until no new transitions have fired.
	      
	          	 This method prevents itself from looping endlessly on a loop in the state
	          machine by only considering transitions that have not fired before in
	          this poll.
	          "
	shared void poll() {
		variable value stillNewTransitionsFiring = true;
		value transitionsThatHaveFiredBefore = HashSet<Transition<State,Event,Priority>>();
		
		while (stillNewTransitionsFiring) {
			stillNewTransitionsFiring = false;
			MutableSet<State> statesToExit = HashSet<State>();
			MutableSet<Transition<State,Event,Priority>> transitionsToFire = HashSet<Transition<State,Event,Priority>>();
			MutableSet<State> statesToEnter = HashSet<State>();
			
			for (sourceState in activeStates) {
				variable Priority? firingPriority = null;
				for (Transition<State,Event,Priority> transition in findTransitionsForState(sourceState)) {
					if (!transitionsThatHaveFiredBefore.contains(transition)) {
						if (exists priority = firingPriority) {
							if (!transition.priority.equals(priority)) {
								/* We reached a lower prio while higher prio transitions are firing.
																										 Don't consider these anymore, go to the next source state. */
								break;
							}
						}
						if (transition.conditions.met) {
							statesToExit.add(sourceState);
							transitionsToFire.add(transition);
							statesToEnter.add(transition.destinationState);
							firingPriority = transition.priority;
						}
					}
				}
			}
			
			statesToExit.each(exitState);
			
			for (transitionToFire in transitionsToFire) {
				executeActions(transitionToFire.actions);
				transitionsThatHaveFiredBefore.add(transitionToFire);
				stillNewTransitionsFiring = true;
			}
			
			statesToEnter.each(enterState);
		}
	}
	
	void executeActions(Actions actions) {
		actions.execute();
	}
	
	void exitState(State state) {
		log.debug("exit state ``state``");
		if (activeStates.contains(state)) {
			executeExitActions(state);
			activeStates.remove(state);
		}
	}
	
	void enterState(State newState) {
		if (endStates.contains(newState)) {
			log.debug("enter end state ``newState``");
			executeEntryActions(newState);
			if (activeStates.empty) {
				log.debug("machine is finished");
			}
			return;
		}
		if (activeStates.add(newState)) {
			log.debug("enter state ``newState``");
			executeEntryActions(newState);
			resetTransitions(newState);
		}
	}
	
	void resetTransitions(State sourceState) {
		value transitionsForState = transitions.get(sourceState);
		if (exists transitionsForState) {
			for (transition in transitionsForState) {
				transition.conditions.reset();
			}
		}
	}
	
	PriorityQueue<Transition<State,Event,Priority>> findTransitionsForState(State sourceState) {
		return transitions.get(sourceState) else newQueue();
	}
	
	PriorityQueue<Transition<State,Event,Priority>> newQueue() => PriorityQueue<Transition<State,Event,Priority>> {
		compare = (Transition<State,Event,Priority> first, Transition<State,Event,Priority> second) => first.compare(second);
	};
	
	void executeExitActions(State state) => executeActions(exitEvents.get(state) else noActions);
	
	void executeEntryActions(State state) => executeActions(entryEvents.get(state) else noActions);
	
	"Gives access to the internals of the state machine."
	shared class Internals() {
		"Returns the end states."
		shared MutableSet<State> endStates => outer.endStates;
		
		"Returns the start states."
		shared MutableSet<State> startStates => outer.startStates;
		
		"Returns the states that have outgoing transitions defined."
		shared Collection<State> sourceStates => outer.transitions.keys;
		
		"Returns the outgoing transitions for a source state."
		shared PriorityQueue<Transition<State,Event,Priority>> transitionsForSourceState(State sourceState) => outer.findTransitionsForState(sourceState);
		
		// TODO complete meta information
		
		"Add 0 or more actions to be executed when the state is exited."
		shared void addExitActions(State state, Action+ action) {
			log.debug("Create exit action for '``state``' (``action``) ");
			if (!exitEvents.defines(state)) {
				exitEvents.put(state, Actions(*action));
				return;
			}
			exitEvents.get(state)?.add(*action);
		}
		
		"Add 0 or more actions to be executed when the state is entered."
		shared void addEntryActions(State state, Action+ action) {
			log.debug("Create entry action for '``state``' (``action``) ");
			if (!entryEvents.defines(state)) {
				entryEvents.put(state, Actions(*action));
				return;
			}
			entryEvents.get(state)?.add(*action);
		}
		
		"Add an end state."
		shared void addEndState(State endState) {
			log.debug("Add end state '``endState``'");
			outer.endStates.add(endState);
		}
		
		"Add a transition."
		shared void addTransition(Transition<State,Event,Priority> transition) {
			value sourceState = transition.sourceState;
			log.debug("Create transition from '``sourceState``' to '``transition.destinationState``' (pre: '``transition.conditions``', action: '``transition.actions``')");
			if (!transitions.defines(sourceState)) {
				transitions.put(sourceState, newQueue());
			}
			transitions.get(sourceState)?.offer(transition);
		}
		
		"Adds a start state, and immediately activates it."
		shared void addStartState(State startState) {
			log.debug("Add start state '``startState``'");
			startStates.add(startState);
			outer.activeStates.add(startState);
		}
		
		"Returns the statemachine whose internals these are."
		shared StateMachine<State,Event,Priority> stateMachine => outer;
	}
}
