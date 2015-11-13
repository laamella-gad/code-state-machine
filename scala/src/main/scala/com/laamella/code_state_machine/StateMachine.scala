package com.laamella.code_state_machine

import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

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
 * @tparam T
 * State type. Each state should have a single instance of this type.
 * An enum is a good fit.
 * @tparam E
 * Event type. Events come into the state machine from the outside
 * world, and are used to trigger state transitions.
 * @tparam P
 * Priority type. Will be used to give priorities to transitions.
 * Enums and Integers are useful here.
 */
class StateMachine[T, E, P <: Ordered[P]] {
  private val log: Logger = LoggerFactory.getLogger("StateMachine")

  private val startStates = mutable.HashSet[T]()
  private val endStates = mutable.HashSet[T]()
  private val activeStates = mutable.HashSet[T]()
  private val exitEvents = mutable.HashMap[T, Actions]()
  private val entryEvents = mutable.HashMap[T, Actions]()
  private val transitions = mutable.HashMap[T, mutable.PriorityQueue[Transition[T, E, P]]]()

  log.debug("New Machine")

  /**
   * Resets all active states to the start states.
   */
  def reset() {
    log.debug("reset()")
    if (startStates.isEmpty) {
      log.warn("State machine does not contain any start states.")
    }
    activeStates.clear()
    for (startState <- startStates) {
      enterState(startState)
    }
  }

  /**
   * @return a set of all active states.
   */
  def getActiveStates = activeStates

  /**
   * @return whether state is currently active.
   */
  def isActive(state: T): Boolean = activeStates.contains(state)


  /**
   * @return whether no states are active. Can be caused by all active states
   *         having disappeared into end states, or by having no start states
   *         at all.
   */
  def isFinished = activeStates.isEmpty


  /**
   * Handle an event coming from the user application. After sending the event
   * to all transitions that have an active source state, poll() will be
   * called.
   *
   * @param event
	 * some event that has happened.
   */
  def handleEvent(event: E) {
    log.debug("handle event {}", event)

    for (sourceState <- activeStates) {
      for (transition <- findTransitionsForState(sourceState)) {
        transition.conditions.handleEvent(event)
      }
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
        for (transition <- findTransitionsForState(sourceState)) {
          if (!transitionsThatHaveFiredBefore.contains(transition)) {
            if (firingPriority != null && !transition.priority.equals(firingPriority)) {
              // We reached a lower prio while higher prio transitions are firing.
              // Don't consider these anymore, go to the next source state.

              // TODO
              //							break;
            } else
            if (transition.conditions.isMet) {
              statesToExit.add(sourceState)
              transitionsToFire.add(transition)
              statesToEnter.add(transition.destinationState)
              firingPriority = Some(transition.priority)
            }
          }
        }
      }

      for (stateToExit <- statesToExit) {
        exitState(stateToExit)
      }
      for (transitionToFire: Transition[T, E, P] <- transitionsToFire) {
        executeActions(transitionToFire.actions)
        transitionsThatHaveFiredBefore.add(transitionToFire)
        stillNewTransitionsFiring = true
      }
      for (stateToEnter <- statesToEnter) {
        enterState(stateToEnter)
      }

    } while (stillNewTransitionsFiring)
  }

  private def executeActions(actions: Actions) {
    if (actions != null) {
      actions.execute()
    }
  }

  private def exitState(state: T) {
    log.debug("exit state {}", state)
    if (activeStates.contains(state)) {
      executeExitActions(state)
      activeStates.remove(state)
    }
  }

  private def enterState(newState: T) {
    if (endStates.contains(newState)) {
      log.debug("enter end state {}", newState)
      executeEntryActions(newState)
      if (activeStates.isEmpty) {
        log.debug("machine is finished")
      }
      return
    }
    if (activeStates.add(newState)) {
      log.debug("enter state {}", newState)
      executeEntryActions(newState)
      resetTransitions(newState)
    }
  }

  private def resetTransitions(sourceState: T) {
    for (transition <- findTransitionsForState(sourceState)) {
      transition.conditions.reset()
    }
  }

  private def findTransitionsForState(sourceState: T): mutable.PriorityQueue[Transition[T, E, P]] = transitions(sourceState)

  private def executeExitActions(state: T) {
    executeActions(exitEvents(state))
  }

  private def executeEntryActions(state: T) {
    executeActions(entryEvents(state))
  }

  /**
   * Gives access to the internals of the state machine.
   */
  class Internals {
    /**
     * @return the end states.
     */
    def getEndStates: mutable.Set[T] = new mutable.HashSet ++= StateMachine.this.endStates

    /**
     * @return the start states.
     */
    def getStartStates: mutable.Set[T] = new mutable.HashSet ++= StateMachine.this.startStates

    /**
     * @return the states that have outgoing transitions defined.
     */
    def getSourceStates: mutable.Set[T] = new mutable.HashSet ++= StateMachine.this.transitions.keySet

    /**
     * @return the outgoing transitions for a source state.
     */
    def getTransitionsForSourceState(sourceState: T) = StateMachine.this.findTransitionsForState(sourceState)


    // TODO complete meta information

    /**
     * Add 0 or more actions to be executed when the state is exited.
     */
    def addExitActions(state: T, action: Seq[Action]) {
      log.debug("Create exit action for '{}' ({}) ", state, action)
      if (!exitEvents.contains(state)) {
        exitEvents.put(state, new Actions(action: _*))
        return
      }
      exitEvents(state).add(action: _*)
    }

    /**
     * Add 0 or more actions to be executed when the state is entered.
     */
    def addEntryActions(state: T, action: Seq[Action]) {
      log.debug("Create entry action for '{}' ({}) ", state, action)
      if (!entryEvents.contains(state)) {
        entryEvents.put(state, new Actions(action: _*))
        return
      }
      entryEvents(state).add(action: _*)
    }

    /**
     * Add an end state.
     */
    def addEndState(endState: T) {
      log.debug("Add end state '{}'", endState)
      endStates.add(endState)
    }

    /**
     * Add a transition.
     */
    def addTransition(transition: Transition[T, E, P]) {
      val sourceState = transition.sourceState
      log.debug("Create transition from '{}' to '{}' (pre: '{}', action: '{}')", Array(sourceState,
        transition.destinationState, transition.conditions, transition.actions))
      if (!transitions.contains(sourceState)) {
        transitions.put(sourceState, mutable.PriorityQueue[Transition[T, E, P]]())
      }
      transitions(sourceState) += transition
    }

    /**
     * Adds a start state, and immediately activates it.
     */
    def addStartState(startState: T) {
      log.debug("Add start state '{}'", startState)
      startStates.add(startState)
      activeStates.add(startState)
    }

    /**
     * @return the statemachine whose internals these are.
     */
    def getStateMachine: StateMachine[T, E, P] = StateMachine.this

  }

}
