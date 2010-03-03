package com.laamella.code_state_machine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Very basic state machine.
 * 
 * @param <T>
 *            enum of states
 * @param <E>
 *            event type
 */
public class Machine<T extends Enum<?>, E> {
	private final T startState;
	private T activeState;
	private final Map<T, Action> exitEvents = new HashMap<T, Action>();
	private final Map<T, Action> entryEvents = new HashMap<T, Action>();
	private final Map<T, Set<Transition<T, E>>> transitions = new HashMap<T, Set<Transition<T, E>>>();

	public Machine(final T startState) {
		this.startState = startState;
		reset();
	}

	public void reset() {
		enterState(startState);
	}

	private void exitState() {
		executeExitAction(activeState);
	}

	private void enterState(final T newState) {
		activeState = newState;
		executeEntryAction(activeState);
	}

	private void makeTransition(final T sourceState, final T destinationState,
			final E event) {
		final Transition<T, E> transition = findTransition(sourceState,
				destinationState);
		transition.execute(event);
	}

	private Transition<T, E> findTransition(final T sourceState,
			final T destinationState) {
		final Set<Transition<T, E>> localTransitions = findTransitionsForState(sourceState);
		if (localTransitions != null) {
			for (final Transition<T, E> localTransition : localTransitions) {
				if (localTransition.getDestinationState() == destinationState) {
					return localTransition;
				}
			}
		}
		return null;
	}

	private Set<Transition<T, E>> findTransitionsForState(final T sourceState) {
		return transitions.get(sourceState);
	}

	public T getActiveState() {
		return activeState;
	}

	public void handleEvent(final E event) {
		for (final Transition<T, E> transition : findTransitionsForState(activeState)) {
			if (transition.isValidTransitionForEvent(event)) {
				transition.execute(event);
				break;
			}
		}
	}

	private void executeExitAction(final T state) {
		final Action action = exitEvents.get(state);
		if (action != null) {
			action.execute();
		}
	}

	private void executeEntryAction(final T state) {
		final Action action = entryEvents.get(state);
		if (action != null) {
			action.execute();
		}
	}

}
