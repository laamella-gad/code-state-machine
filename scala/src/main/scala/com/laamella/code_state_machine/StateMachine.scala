package com.laamella.code_state_machine

import grizzled.slf4j.Logging

import scala.collection.mutable

/**
  * A condition that is met or not.
  *
  * E is the event type.
  */
trait Condition[E] {
  /**
    * Handle an event.
    *
    * @param event the event that has occurred.
    */
  def handleEvent(event: E)

  /**
    * @return whether the condition is met.
    */
  def isMet: Boolean

  /**
    * This method is called every time the sourceState for this transition is
    * entered. It can be used to implement stateful transitions, like
    * transitions that fire after a certain amount of time.
    */
  def reset()
}

/**
  * A conditional transition between two states.
  *
  * @tparam T type of state.
  * @tparam E type of event.
  * @tparam P type of priority.
  */
class Transition[T, E, P <: Ordered[P]](
                                         val sourceState: T,
                                         val destinationState: T,
                                         val conditions: Seq[Condition[E]],
                                         val priority: P,
                                         val actions: Seq[() => Unit]) extends Ordered[Transition[T, E, P]] {
  override def toString = s"Transition from $sourceState to $destinationState, condition $conditions, action $actions, priority $priority"

  /** Compares transitions on their priorities. */
  override def compare(that: Transition[T, E, P]): Int = priority.compareTo(that.priority)
}

/**
  * A programmer friendly state machine.
  * <p/>
  * Features:
  * <ul>
  * <li>It is non-deterministic, but has the tools to become deterministic.</li>
  * <li>It allows multiple start states.</li>
  * <li>It allows multiple active states.</li>
  * <li>It allows multiple end states.</li>
  * <li>States and their transitions do not have to form a single graph. Separate
  * graphs may exist inside a single state machine.</li>
  * <li>Each state has a chain of entry and exit actions.</li>
  * <li>Each transition has a chain of actions.</li>
  * <li>It does not do any kind of compilation.</li>
  * <li>Its code is written in a straightforward way, and is hopefully easy to
  * understand.</li>
  * <li>It has a priority system for transitions.</li>
  * <li>It does not have sub state machines; a state machine is not a state.</li>
  * <li>It has transitions that use a state machine for their condition.</li>
  * <li>With the DSL, transitions to a certain state can be added for multiple
  * source states, thereby faking global transitions.</li>
  * <li>It tries to put as few constraints as possible on the user.</li>
  * <li>It has only one dependency: slf4j for logging, which can be configured to
  * use any other logging framework.</li>
  * <li>The state type can be anything.</li>
  * <li>The event type can be anything.</li>
  * <li>The priority type can be anything as long as it's Ordered.</li>
  * <li>It has two, always accessible modes of usage: asking the state machine
  * for the current state, or having the state machine trigger actions that
  * change the user code state.
  * </ul>
  *
  * @param startStates the states that are active at startup, or after a reset()
  * @param endStates a state that can be reached, but is never added to the active states. The machine is finished when no states are active, which can be achieved by having the active states transition to an end state.
  * @param exitEvents triggered when a specific state is exited
  * @param entryEvents triggered when a specific state is entered
  * @param transitions all the transitions, structured as "starting from this event, a sequence of possible transitions in order of priority"
  *
  * @tparam T State type. Each state should have a single instance of this type.
  *           An enum is a good fit.
  * @tparam E Event type. Events come into the state machine from the outside
  *           world, and are used to trigger state transitions.
  * @tparam P Priority type. Will be used to give priorities to transitions.
  *           Enums and Integers are useful here.
  */
class StateMachine[T, E, P <: Ordered[P]](
                                           val startStates: Set[T],
                                           val endStates: Set[T],
                                           val exitEvents: Map[T, Seq[() => Unit]],
                                           val entryEvents: Map[T, Seq[() => Unit]],
                                           val transitions: Map[T, Seq[Transition[T, E, P]]]) extends Logging {

  // TODO change to immutable
  val activeStates = mutable.HashSet[T]()

  debug("New Machine")
  reset()

  /**
    * Resets all active states to the start states.
    */
  def reset() {
    debug("reset()")
    if (startStates.isEmpty) {
      warn("State machine does not contain any start states.")
    }
    activeStates.clear()
    startStates.foreach(enterState)
  }

  /**
    * @return whether state is currently active.
    */
  def active(state: T): Boolean = activeStates.contains(state)

  /**
    * @return whether no states are active. Can be caused by all active states
    *         having disappeared into end states, or by having no start states
    *         at all.
    */
  def finished = activeStates.isEmpty

  /**
    * Handle an event coming from the user application. After sending the event
    * to all transitions that have an active source state, poll() will be
    * called.
    *
    * @param event some event that has happened.
    */
  def handleEvent(event: E) {
    debug(s"handle event $event")

    for (sourceState <- activeStates;
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
  def poll() {
    var stillNewTransitionsFiring = true
    val transitionsThatHaveFiredBefore = mutable.HashSet[Transition[T, E, P]]()

    do {
      stillNewTransitionsFiring = false
      val statesToExit = mutable.HashSet[T]()
      val transitionsToFire = mutable.HashSet[Transition[T, E, P]]()
      val statesToEnter = mutable.HashSet[T]()

      for (sourceState <- activeStates) {
        var firingPriority: Option[P] = None
        for (transition <- transitions(sourceState)) {
          if (!transitionsThatHaveFiredBefore.contains(transition)) {
            // TODO put in 1 expression
            if (firingPriority.isDefined && transition.priority != firingPriority.get) {
              // We reached a lower prio while higher prio transitions are firing.
              // Don't consider these anymore, go to the next source state.

              // TODO
              //							break;
            } else if (transition.conditions.forall(_.isMet)) {
              statesToExit.add(sourceState)
              transitionsToFire.add(transition)
              statesToEnter.add(transition.destinationState)
              firingPriority = Some(transition.priority)
            }
          }
        }
      }

      statesToExit.foreach(exitState)

      for (transitionToFire: Transition[T, E, P] <- transitionsToFire) {
        transitionToFire.actions.foreach(_ ())
        transitionsThatHaveFiredBefore += transitionToFire
        stillNewTransitionsFiring = true
      }

      statesToEnter.foreach(enterState)

    } while (stillNewTransitionsFiring)
  }

  private def exitState(state: T) {
    debug(s"exit state $state")
    if (activeStates.contains(state)) {
      executeExitActions(state)
      activeStates.remove(state)
    }
  }

  private def enterState(newState: T) {
    if (endStates.contains(newState)) {
      debug(s"enter end state $newState")
      executeEntryActions(newState)
      if (activeStates.isEmpty) {
        debug(s"machine is finished")
      }
      return
    }
    if (activeStates.add(newState)) {
      debug(s"enter state $newState")
      executeEntryActions(newState)
      resetTransitions(newState)
    }
  }

  private def resetTransitions(sourceState: T) {
    transitions.get(sourceState).foreach(_.foreach(_.conditions.foreach(_.reset())))
  }

  private def executeExitActions(state: T) {
    exitEvents.get(state).foreach(_.foreach(_ ()))
  }

  private def executeEntryActions(state: T) {
    entryEvents.get(state).foreach(_.foreach(_ ()))
  }
}
