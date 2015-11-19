package com.laamella.code_state_machine.condition

import com.laamella.code_state_machine.StateMachine

/**
 * This condition is met when all states passed in the constructor are active.
 */
final class StatesInactiveCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P], statesThatMustBeInactive: T*) extends NonEventBasedCondition[E] {
  override def isMet: Boolean = {
    // TODO there is a better way to express this
    for (stateThatMustBeInactive <- statesThatMustBeInactive) {
      if (stateMachine.active(stateThatMustBeInactive)) {
        return false
      }
    }
    true
  }
}
