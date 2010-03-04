package com.laamella.code_state_machine;

import java.util.Set;

public interface StateMachineBuilder<T, E> {
	// Building
	void addStartState(T startState);

	void setExitAction(T state, Action<E> action);

	void setEntryAction(T state, Action<E> action);

	void addEndState(T endState);

	void addTransition(Transition<T, E> transition);

	StateMachine<T, E> build();

	// Introspection
	Set<T> getEndStates();

	Set<T> getStartStates();

	Set<T> getSourceStates();

	Set<Transition<T, E>> getTransitionsForSourceState(T sourceState);

}