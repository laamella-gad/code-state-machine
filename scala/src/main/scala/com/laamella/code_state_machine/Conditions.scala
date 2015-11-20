package com.laamella.code_state_machine

/**
  * This condition is met after a certain amount of milliseconds.
  */
class AfterCondition[E](milliseconds: Long) extends NonEventBasedCondition[E] {
  private var minimalMeetTime: Long = _

  override def isMet = System.currentTimeMillis() > minimalMeetTime

  override def reset() = minimalMeetTime = System.currentTimeMillis() + milliseconds
}


/**
  * This condition is always met.
  */
final class AlwaysCondition[E] extends NonEventBasedCondition[E] {
  /** @return whether the condition is met. */
  override def isMet: Boolean = true

  override def toString = "always"
}

/**
  * A base class for conditions that are met depending on some kind of event
  * handling.
  */
abstract class EventBasedCondition[E] extends Condition[E] {
  private var met = false

  override def isMet = met

  override def reset() = met = false

  override def handleEvent(event: E) = {
    if (!met && conditionIsMetAfterHandlingEvent(event)) {
      met = true
    }
  }

  protected def conditionIsMetAfterHandlingEvent(event: E): Boolean
}

/**
  * This condition is met when the event is equal to one of the events passed in
  * the constructor.
  */
final class MultiEventMatchCondition[E](matchEvents: E*) extends EventBasedCondition[E] {
  override def toString = {
    val str = new StringBuilder("one of (")
    matchEvents.foreach(matchEvent => str.append(matchEvent.toString).append(" "))
    str.append(")").toString()
  }

  override protected def conditionIsMetAfterHandlingEvent(event: E) = matchEvents contains event
}


/**
  * This condition is never met, and as such blocks a transition from ever
  * firing. Probably only useful in test scenarios.
  */
final class NeverCondition[E] extends NonEventBasedCondition[E] {
  override def isMet = false

  override def toString = "never"
}

/**
  * This condition is met when the event is equal to the event passed in the
  * constructor.
  */
final class SingleEventMatchCondition[E](singleEvent: E) extends EventBasedCondition[E] {
  override def toString = s"is $singleEvent"

  override protected def conditionIsMetAfterHandlingEvent(event: E) = singleEvent equals event
}

/** A base class for conditions that do not respond to events. */
abstract class NonEventBasedCondition[E] extends Condition[E] {
  // Not event based, so not used.
  override def handleEvent(event: E): Unit = Unit

  // Does nothing by default.
  override def reset(): Unit = Unit
}

/** This condition is met when all states passed in the constructor are active. */
final class StatesActiveCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P], statesThatMustBeActive: T*) extends NonEventBasedCondition[E] {
  override def isMet = statesThatMustBeActive.forall(stateMachine.active)
}

/** This condition is met when all states passed in the constructor are active. */
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

/**
  * A condition that acts as a kind of sub-statemachine. The condition is met
  * when the embedded statemachine has no active states left.
  *
  * @param stateMachine the state machine to use. Note that using the same state
  *                     machine for multiple conditions will not magically clone it,
  *                     it still is the same machine with the same state in all
  *                     conditions.
  * @tparam E event type. The same type as the parent state machine.
  */
// TODO test
final class SubStateMachineCondition[T, E, P <: Ordered[P]](stateMachine: StateMachine[T, E, P]) extends EventBasedCondition[E] {
  override def conditionIsMetAfterHandlingEvent(event: E): Boolean = {
    stateMachine.handleEvent(event)
    stateMachine.finished
  }

  override def reset() = {
    super.reset()
    stateMachine.reset()
  }
}
