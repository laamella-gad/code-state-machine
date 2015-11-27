package com.laamella.code_state_machine

import grizzled.slf4j.Logging

import scala.collection.mutable

/**
  * A condition that is met or not.
  *
  * Event is the event type.
  */
trait Condition[State, Event] {
  /**
    * Handle an event.
    *
    * @param event the event that has occurred.
    */
  def handleEvent(event: Event): Unit

  /**
    * Decide and return whether the condition is met.
    */
  def evaluate(activeStates: Set[State]): Boolean

  /**
    * This method is called every time the sourceState for this transition is
    * entered. It can be used to implement stateful transitions, like
    * transitions that fire after a certain amount of time.
    */
  def reset()
}

/**
  * A conditional transition between two states.
  */
class Transition[State, Event, Priority <: Ordered[Priority]](
                                                               val sourceState: State,
                                                               val destinationState: State,
                                                               val conditions: Seq[Condition[Event]],
                                                               val priority: Priority,
                                                               val actions: Seq[() => Unit]) extends Ordered[Transition[State, Event, Priority]] {
  override def toString = s"Transition from $sourceState to $destinationState, condition $conditions, action $actions, priority $priority"

  /** Compares transitions on their priorities. */
  override def compare(that: Transition[State, Event, Priority]): Int = priority.compareTo(that.priority)
}

/**
  * Build this machine by using one of the builders, not by using this constructor.
  *
  * @param startStates the states that are active at startup, or after a reset()
  * @param endStates a state that can be reached, but is never added to the active states. The machine is finished when no states are active, which can be achieved by having the active states transition to an end state.
  * @param exitEvents triggered when a specific state is exited
  * @param entryEvents triggered when a specific state is entered
  * @param transitions all the transitions, structured as "starting from this event, a sequence of possible transitions in order of priority"
  */
class StateMachine[State, Event, Priority <: Ordered[Priority]](
                                                                 val startStates: Set[State],
                                                                 val endStates: Set[State],
                                                                 val exitEvents: Map[State, Seq[() => Unit]],
                                                                 val entryEvents: Map[State, Seq[() => Unit]],
                                                                 val transitions: Map[State, Seq[Transition[State, Event, Priority]]]) extends Logging {

  // TODO change to immutable
  private var _activeStates = Set[State]()

  /** The currently active states. Updated with a call to poll() */
  def activeStates: Set[State] = _activeStates

  debug("New Machine")
  reset()

  /**
    * Resets all active states to the start states.
    */
  def reset(): Unit = {
    debug("reset()")
    if (startStates.isEmpty) {
      warn("State machine does not contain any start states.")
    }
    _activeStates = Set()
    startStates.foreach(enterState)
  }

  /**
    * Return whether state is currently active.
    */
  def active(state: State): Boolean = _activeStates.contains(state)

  /**
    * Return whether no states are active. Can be caused by all active states
    * having disappeared into end states, or by having no start states
    * at all.
    */
  def finished = _activeStates.isEmpty

  /**
    * Handle an event coming from the user application. After sending the event
    * to all transitions that have an active source state, poll() will be
    * called.
    *
    * @param event some event that has happened.
    */
  def handleEvent(event: Event): Unit = {
    debug(s"handle event $event")

    for (sourceState <- _activeStates;
         transition <- transitions(sourceState)) {
      transition.conditions.foreach(_.handleEvent(event))
    }
    poll()
  }

  /**
    * Tells the state machine to look for state changes to execute. This method
    * has to be called regularly, or the state machine will do nothing at all.
    * <ul>
    * <li>Repeat...</li>
    * <ol>
    * <li>For all transitions that have an active source state, find the
    * transitions that will fire.</li>
    * <ul>
    * <li>Ignore transitions that have already fired in this poll().</li>
    * <li>For a single source state, find the transition of the highest
    * priority which will fire (if any fire at all.) If multiple transitions
    * share this priority, fire them all.</li>
    * </ul>
    * <li>For all states that will be exited, fire the exit state event.</li>
    * <li>For all transitions that fire, fire the transition action.</li>
    * <li>For all states that will be entered, fire the entry state event.</li>
    * </ol>
    * <li>... until no new transitions have fired.</li>
    * </ul>
    * <p/>
    * This method prevents itself from looping endlessly on a loop in the state
    * machine by only considering transitions that have not fired before in
    * this poll.
    */
  def poll(): Unit = {
    var stillNewTransitionsFiring = true
    val transitionsThatHaveFiredBefore = mutable.HashSet[Transition[State, Event, Priority]]()

    do {
      stillNewTransitionsFiring = false
      val statesToExit = mutable.HashSet[State]()
      val transitionsToFire = mutable.HashSet[Transition[State, Event, Priority]]()
      val statesToEnter = mutable.HashSet[State]()

      for (sourceState <- _activeStates) {
        var firingPriority: Option[Priority] = None
        // Check all possible transitions from one active state.
        for (transition <- transitions(sourceState)) {
          // Never fire a transition more than once in one poll.
          if (!transitionsThatHaveFiredBefore.contains(transition)) {
            // Don't fire lower priorities for a state that already has higher prio transitions firing.
            // This takes advantage of the transitions being in order of priority.
            if (firingPriority.isEmpty || firingPriority.contains(transition.priority)) {
              // Only fire if all conditions are met.
              if (transition.conditions.forall(_.evaluate)) {
                statesToExit += sourceState
                transitionsToFire += transition
                statesToEnter += transition.destinationState
                firingPriority = Some(transition.priority)
              }
            }
          }
        }
      }

      statesToExit.foreach(exitState)

      for (transitionToFire: Transition[State, Event, Priority] <- transitionsToFire) {
        transitionToFire.actions.foreach(_ ())
        transitionsThatHaveFiredBefore += transitionToFire
        stillNewTransitionsFiring = true
      }

      statesToEnter.foreach(enterState)

      if (_activeStates.isEmpty) {
        debug(s"machine is finished")
      }

    } while (stillNewTransitionsFiring)
  }

  private def exitState(state: State): Unit = {
    debug(s"exit state $state")
    if (_activeStates.contains(state)) {
      executeExitActions(state)
      _activeStates = _activeStates - state
    }
  }

  private def enterState(newState: State): Unit = {
    if (endStates.contains(newState)) {
      debug(s"enter end state $newState")
      executeEntryActions(newState)
    } else if (!_activeStates.contains(newState)) {
      debug(s"enter state $newState")
      _activeStates = _activeStates + newState
      executeEntryActions(newState)
      resetTransitions(newState)
    }
  }

  private def resetTransitions(sourceState: State): Unit = {
    transitions.get(sourceState).foreach(_.foreach(_.conditions.foreach(_.reset())))
  }

  private def executeExitActions(state: State): Unit = {
    exitEvents.get(state).foreach(_.foreach(_ ()))
  }

  private def executeEntryActions(state: State): Unit = {
    entryEvents.get(state).foreach(_.foreach(_ ()))
  }
}
