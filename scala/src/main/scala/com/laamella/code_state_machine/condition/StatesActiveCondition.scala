package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.StateMachine

/**
 * This condition is met when all states passed in the constructor are active.
 */
final class StatesActiveCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P], statesThatMustBeActive: T*) extends NonEventBasedCondition[E] {
  override def isMet = statesThatMustBeActive.forall(stateMachine.getActiveStates contains)
}