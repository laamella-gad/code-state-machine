import ceylon.logging {
	logger,
	Logger
}
import ceylon.time {
	systemTime
}

import java.lang {
	Runnable,
	Thread
}

Logger log = logger(`package laamella.code_state_machine`);

"An action which finishes at some time in the future. A transition can wait for the action to be finished by using the isFinished condition."
shared interface FinishableAction<Event> satisfies Action {
	shared formal Condition<Event> finished;
}

"This action logs a line."
shared final class LogAction(String logText) satisfies Action {
	
	shared actual void execute() {
		log.trace(logText);
	}
	
	shared actual String string = "log (" + logText + ")";
}

"This action starts a separate work thread with user code. A transition can wait for this work to be finished by using the finished condition."
// TODO test
shared abstract class TaskAction<Event>() satisfies Runnable & FinishableAction<Event> {
	variable Thread? taskThread = null;
	variable NonEventBasedCondition<Event> finishedCondition = NeverCondition<Event>();
	
	shared actual void execute() {
		Thread thread = Thread(this);
		taskThread = thread;
		// TODO weird
		class X() extends NonEventBasedCondition<Event>() {
			shared actual Boolean met => thread.state == Thread.State.\iTERMINATED;
		}
		finishedCondition = X();
		
		thread.start();
	}
	
	shared actual Condition<Event> finished => finishedCondition;
}


"A base class for conditions that do not respond to events."
shared abstract class NonEventBasedCondition<Event>() satisfies Condition<Event> {
	shared actual default void handleEvent(Event event) {
		// Not event based, so not used.
	}
	
	shared actual default void reset() {
		// Does nothing by default.
	}
}

"A base class for conditions that are met depending on some kind of event handling."
shared abstract class EventBasedCondition<Event>() satisfies Condition<Event> {
	shared actual variable Boolean met = false;
	
	shared actual default void reset() {
		met = false;
	}
	
	shared actual void handleEvent(Event event) {
		if (!met && conditionIsMetAfterHandlingEvent(event)) {
			met = true;
		}
	}
	
	"Decides whether the condition is met based on the event received."
	shared formal Boolean conditionIsMetAfterHandlingEvent(Event event);
}

"This condition is met when the event is equal to one of the events passed in the constructor."
shared class EventMatchCondition<Event>({Event+} events) extends EventBasedCondition<Event>()
		given Event satisfies Object {
	shared actual String string {
		if (events.size == 1) {
			return events.first.string;
		}
		value str = StringBuilder();
		str.append("one of (");
		for (matchEvent in events) {
			str.append(matchEvent.string + " ");
		}
		return str.append(")").string;
	}
	
	shared actual Boolean conditionIsMetAfterHandlingEvent(Event event) => events.contains(event);
}

"This condition is met when all states passed in the constructor are active."
shared class StatesActiveCondition<T, E, P>(StateMachine<T,E,P> stateMachine, {T+} statesThatMustBeActive) extends NonEventBasedCondition<E>()
		given P satisfies Comparable<P>
		given T satisfies Object
		given E satisfies Object {
	shared actual Boolean met => stateMachine.activeStates.containsEvery(statesThatMustBeActive);
}

"This condition is met when all states passed in the constructor are active."
shared class StatesInactiveCondition<T, E, P>(StateMachine<T,E,P> stateMachine, {T+} statesThatMustBeInactive) extends NonEventBasedCondition<E>()
		given P satisfies Comparable<P>
		given T satisfies Object
		given E satisfies Object {
	shared actual Boolean met {
		for (stateThatMustBeInactive in statesThatMustBeInactive) {
			if (stateMachine.activeStates.contains(stateThatMustBeInactive)) {
				return false;
			}
		}
		return true;
	}
}

"A condition that acts as a kind of sub-statemachine. 
 The condition is met  when the embedded statemachine has no active states left.
 
 Supply the state machine to use. 
 Note that using the same state machine for multiple conditions will not magically clone it, 
 it still is the same machine with the same state in all conditions."
// TODO test
shared class SubStateMachineCondition<T, E, P>(StateMachine<T,E,P> stateMachine) extends EventBasedCondition<E>()
		given P satisfies Comparable<P>
		given T satisfies Object
		given E satisfies Object {
	
	shared actual Boolean conditionIsMetAfterHandlingEvent(E event) {
		stateMachine.handleEvent(event);
		return stateMachine.finished;
	}
	
	shared actual void reset() {
		super.reset();
		stateMachine.reset();
	}
}


"This condition is never met, and as such blocks a transition from ever firing. Probably only useful in test scenarios."
shared class NeverCondition<Event>() extends NonEventBasedCondition<Event>() {
	shared actual Boolean met = false;
	shared actual String string = "never";
}

"This condition is always met."
shared class AlwaysCondition<Event>() extends NonEventBasedCondition<Event>() {
	shared actual Boolean met = true;
	shared actual String string = "always";
}

"This condition is met after a certain amount of milliseconds."
shared class AfterCondition<Event>(Integer milliseconds) extends NonEventBasedCondition<Event>() {
	variable Integer minimalMeetTime = 0;
	
	shared actual Boolean met => systemTime.milliseconds() > minimalMeetTime;
	
	shared actual void reset() {
		minimalMeetTime = systemTime.milliseconds() + milliseconds;
	}
}
