package com.laamella.code_state_machine

/**
  * This condition is met after a certain amount of milliseconds.
  */
class AfterCondition[Event](milliseconds: Long) extends NonEventBasedCondition[Event] {
  private var minimalMeetTime: Long = _

  override def isMet = System.currentTimeMillis() > minimalMeetTime

  override def reset() = minimalMeetTime = System.currentTimeMillis() + milliseconds
}


/**
  * This condition is always met.
  */
final class AlwaysCondition[Event] extends NonEventBasedCondition[Event] {
  /** @return whether the condition is met. */
  override def isMet: Boolean = true

  override def toString = "always"
}

/**
  * A base class for conditions that are met depending on some kind of event
  * handling.
  */
abstract class EventBasedCondition[Event] extends Condition[Event] {
  private var met = false

  override def isMet = met

  override def reset() = met = false

  override def handleEvent(event: Event) = {
    if (!met && conditionIsMetAfterHandlingEvent(event)) {
      met = true
    }
  }

  protected def conditionIsMetAfterHandlingEvent(event: Event): Boolean
}

/**
  * This condition is met when the event is equal to one of the events passed in
  * the constructor.
  */
final class MultiEventMatchCondition[Event](matchEvents: Event*) extends EventBasedCondition[Event] {
  override def toString = {
    val str = new StringBuilder("one of (")
    matchEvents.foreach(matchEvent => str.append(matchEvent.toString).append(" "))
    str.append(")").toString()
  }

  override protected def conditionIsMetAfterHandlingEvent(event: Event) = matchEvents contains event
}


/**
  * This condition is never met, and as such blocks a transition from ever
  * firing. Probably only useful in test scenarios.
  */
final class NeverCondition[Event] extends NonEventBasedCondition[Event] {
  override def isMet = false

  override def toString = "never"
}

/**
  * This condition is met when the event is equal to the event passed in the
  * constructor.
  */
final class SingleEventMatchCondition[Event](singleEvent: Event) extends EventBasedCondition[Event] {
  override def toString = s"is $singleEvent"

  override protected def conditionIsMetAfterHandlingEvent(event: Event) = singleEvent == event
}

/** A base class for conditions that do not respond to events. */
abstract class NonEventBasedCondition[Event] extends Condition[Event] {
  // Not event based, so not used.
  override def handleEvent(event: Event): Unit = Unit

  // Does nothing by default.
  override def reset(): Unit = Unit
}

/** This condition is met when all states passed in the constructor are active. */
final class StatesActiveCondition[State, Event, Priority <: Ordered[Priority]](stateMachine: StateMachine[State, Event, Priority], statesThatMustBeActive: State*) extends NonEventBasedCondition[Event] {
  override def isMet = statesThatMustBeActive.forall(stateMachine.active)
}

/** This condition is met when all states passed in the constructor are active. */
final class StatesInactiveCondition[State, Event, Priority <: Ordered[Priority]](stateMachine: StateMachine[State, Event, Priority], statesThatMustBeInactive: State*) extends NonEventBasedCondition[Event] {
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
  * @tparam Event event type. The same type as the parent state machine.
  */
// TODO test
final class SubStateMachineCondition[State, Event, Priority <: Ordered[Priority]](stateMachine: StateMachine[State, Event, Priority]) extends EventBasedCondition[Event] {
  override def conditionIsMetAfterHandlingEvent(event: Event): Boolean = {
    stateMachine.handleEvent(event)
    stateMachine.finished
  }

  override def reset() = {
    super.reset()
    stateMachine.reset()
  }
}
