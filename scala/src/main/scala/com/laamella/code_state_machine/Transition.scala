package com.laamella.code_state_machine

/**
 * A conditional transition between two states.
 *
 * @tparam T type of state.
 * @tparam E type of event.
 * @tparam P type of priority.
 */
class Transition[T, E, P <: Ordered[P]](val sourceState: T, val destinationState: T, val conditions: Conditions[E], val priority: P, val actions: Actions) extends Ordered[Transition[T, E, P]] {
  override def toString = s"Transition from $sourceState to $destinationState, condition $conditions, action $actions, priority $priority"

  /** Compares transitions on their priorities. */
  override def compare(that: Transition[T, E, P]): Int = priority.compareTo(that.priority)
}
