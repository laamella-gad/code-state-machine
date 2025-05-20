package com.laamella.kode_state_machine

/**
 * A conditional transition between two states.
 *
 * @param sourceState the state that must be active for this transition to fire.
 * @param destinationState the state that will be entered when this transition fires.
 * @param conditions the condition that must be met for this transition to fire.
 * @param priority the priority of this transition. If this transitions fires, no lower priority transitions for the same source state are allowed to fire.
 * @param actions the actions that will be executed when this transition fires.
 * @param T type of state.
 * @param E type of event.
 * @param P type of priority.
 */
class Transition<T, E, P : Comparable<P>>(
    val sourceState: T,
    val destinationState: T,
    val conditions: List<Condition<E>>,
    val priority: P,
    val actions: List<Action>
) : Comparable<Transition<T, E, P>> {
    /**
     * Compares transitions on their priorities.
     */
    override fun compareTo(other: Transition<T, E, P>): Int {
        return priority.compareTo(other.priority)
    }

    override fun toString(): String {
        return String.format(
            "Transition from %s to %s, condition %s, action %s, priority %s",
            sourceState,
            destinationState,
            conditions,
            actions,
            priority
        )
    }
}
