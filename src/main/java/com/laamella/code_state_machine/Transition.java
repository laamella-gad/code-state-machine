package com.laamella.code_state_machine;

/**
 * A conditional transition between two states.
 * 
 * @param <T>
 *            type of state.
 * @param <E>
 *            type of event.
 * @param <P>
 *            type of priority.
 */
public class Transition<T, E, P extends Comparable<P>> implements Comparable<Transition<T, E, P>> {
	private final T destinationState;
	private final T sourceState;
	private final Conditions<E> conditions;
	private final Actions action;
	private final P priority;

	public Transition(final T sourceState, final T destinationState, final Conditions<E> conditions, final P priority,
			final Actions actions) {
		assert destinationState != null;
		assert sourceState != null;
		assert conditions != null;
		assert actions != null;
		assert priority != null;

		this.destinationState = destinationState;
		this.sourceState = sourceState;
		this.conditions = conditions;
		this.action = actions;
		this.priority = priority;
	}

	/**
	 * @return the state that will be entered when this transition fires.
	 */
	public final T getDestinationState() {
		return destinationState;
	}

	/**
	 * @return the state that must be active for this transition to fire.
	 */
	public final T getSourceState() {
		return sourceState;
	}

	/**
	 * @return The actions that will be executed when this transition fires.
	 *         Never null.
	 */
	public Actions getActions() {
		return action;
	}

	/**
	 * @return the condition that must be met for this transition to fire.
	 */
	public Conditions<E> getCondition() {
		return conditions;
	}

	/**
	 * Compares transitions on their priorities.
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(final Transition<T, E, P> o) {
		return priority.compareTo(o.getPriority());
	}

	/**
	 * @return the priority of this transition. If this transitions fires, no
	 *         lower priority transitions for the same source state are allowed
	 *         to fire.
	 */
	public P getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return String.format("Transition from %s to %s, condition %s, action %s, priority %s", sourceState,
				destinationState, conditions, action, priority);
	}
}
