package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.StateMachine

/**
 * A condition that acts as a kind of sub-statemachine. The condition is met
 * when the embedded statemachine has no active states left.
 *
 * @param stateMachine
	 * the state machine to use. Note that using the same state
 *   machine for multiple conditions will not magically clone it,
 *   it still is the same machine with the same state in all
 *   conditions.
 * @tparam E event type. The same type as the parent state machine.
 */
// TODO test
final class SubStateMachineCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P]) extends EventBasedCondition[E] {
  override def conditionIsMetAfterHandlingEvent(event: E): Boolean = {
    stateMachine.handleEvent(event)
    stateMachine finished
  }

  override def reset() = {
    super.reset()
    stateMachine.reset()
  }
}
