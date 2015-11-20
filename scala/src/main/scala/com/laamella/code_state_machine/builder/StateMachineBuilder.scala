package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine.{Transition, StateMachine}
import grizzled.slf4j.Logger

import scala.collection.mutable

/**
  * Interface that all builder should adhere to.
  */
class StateMachineBuilder[T, E, P <: Ordered[P]] {
  private lazy val log = Logger(getClass)

  private val startStates = mutable.Set[T]()
  private val endStates = mutable.Set[T]()
  private val exitEvents = mutable.Map[T, Seq[() => Unit]]()
  private val entryEvents = mutable.Map[T, Seq[() => Unit]]()
  private val transitions = mutable.Map[T, mutable.PriorityQueue[Transition[T, E, P]]]()

  /** Adds a start state, and immediately activates it. */
  def addStartState(startState: T): Unit = {
    log.debug(s"Add start state '$startState'")
    startStates += startState
  }

  /** Add 0 or more actions to be executed when the state is exited. */
  def addExitActions(state: T, actions: Seq[() => Unit]): Unit = {
    log.debug(s"Create exit action for '$state' ($actions)")
    // TODO there's probably a better way to express this
    if (!exitEvents.contains(state)) {
      exitEvents += state -> actions
      return
    }
    exitEvents(state) ++= actions
  }

  /** Add 0 or more actions to be executed when the state is entered. */
  def addEntryActions(state: T, actions: Seq[() => Unit]): Unit = {
    log.debug(s"Create entry action for '$state' ($actions)")
    if (!entryEvents.contains(state)) {
      entryEvents += state -> actions
      return
    }
    entryEvents(state) ++= actions
  }

  /** Add an end state. */
  def addEndState(endState: T): Unit = {
    log.debug(s"Add end state '$endState'")
    endStates += endState
  }

  /** Add a transition. */
  def addTransition(transition: Transition[T, E, P]): Unit = {
    val sourceState = transition.sourceState
    log.debug(s"Create transition from '$sourceState' to '${transition.destinationState}' (pre: '${transition.conditions}', action: '${transition.actions}')")
    if (!transitions.contains(sourceState)) {
      transitions.put(sourceState, mutable.PriorityQueue[Transition[T, E, P]]())
    }
    transitions(sourceState) += transition
  }

  def build(): StateMachine[T, E, P] = {
    val immutableStartStates = startStates.toSet
    val immutableEndStates = endStates.toSet
    val immutableEntryEvents = entryEvents.toMap
    val immutableExitEvents = exitEvents.toMap
    val immutableTransitions: Map[T, Seq[Transition[T, E, P]]] = transitions.mapValues(transitionQueue => transitionQueue.toSeq).toMap
    new StateMachine[T, E, P](immutableStartStates, immutableEndStates, immutableExitEvents, immutableEntryEvents, immutableTransitions)
  }
}
