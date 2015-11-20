package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine._
import com.laamella.code_state_machine.action.LogAction
import com.laamella.code_state_machine.condition.{AfterCondition, AlwaysCondition, MultiEventMatchCondition, NeverCondition, SingleEventMatchCondition}

import scala.collection.mutable

/**
  * A pretty "DSL" builder for a state machine.
  */
class DslStateMachineBuilder[T, E, P <: Ordered[P]](defaultPriority: P) {
  private val builder = new StateMachineBuilder[T, E, P]()

  class DefiningState(sourceStates: mutable.Set[T]) {
    def except(states: T*): DefiningState = {
      states.foreach(sourceStates.remove)
      this
    }

    def onExit(action: (() => Unit)*): DefiningState = {
      sourceStates.foreach(builder.addExitActions(_, action))
      this
    }

    def onEntry(action: (() => Unit)*): DefiningState = {
      sourceStates.foreach(builder.addEntryActions(_, action))
      this
    }

    def onExitLog(logText: String): DefiningState = onExit(new LogAction(logText))

    def onEntryLog(logText: String): DefiningState = onEntry(new LogAction(logText))

    def isAnEndState(): DefiningState = {
      sourceStates.foreach(builder.addEndState)
      this
    }

    def isAStartState(): DefiningState = {
      sourceStates.foreach(builder.addStartState)
      this
    }

    def areEndStates(): DefiningState = isAnEndState()

    def areStartStates(): DefiningState = isAStartState()

    def when(conditions: Condition[E]*): DefiningTransition = new DefiningTransition(sourceStates, conditions)

    def always(): DefiningTransition = new DefiningTransition(sourceStates, Seq(new AlwaysCondition[E]()))

    def never(): DefiningTransition = new DefiningTransition(sourceStates, Seq(new NeverCondition[E]()))

    def after(milliseconds: Long): DefiningTransition = new DefiningTransition(sourceStates, Seq(new AfterCondition[E](milliseconds)))

    def onEvents(events: E*): DefiningTransition = {
      val condition = if (events.length == 1) {
        val singleEvent = events(0)
        new SingleEventMatchCondition[E](singleEvent)
      } else {
        new MultiEventMatchCondition[E](events: _*)
      }
      new DefiningTransition(sourceStates, Seq(condition))
    }

    // FIXME find a way to rescue these conditions
    //  def active(statesThatMustBeActive: T*): Condition[E] = {
    //    new StatesActiveCondition[T, E, P](machine, statesThatMustBeActive: _*)
    //  }
    //
    //  def inactive(statesThatMustBeInactive: T*): Condition[E] = {
    //    new StatesInactiveCondition[T, E, P](machine, statesThatMustBeInactive: _*)
    //  }

  }

  class DefiningTransition(sourceStates: mutable.Set[T], conditions: Seq[Condition[E]]) {
    private val actions = mutable.MutableList[() => Unit]()
    private var priority = defaultPriority

    def doing(action: () => Unit): DefiningTransition = {
      actions += action
      this
    }

    def log(logText: String): DefiningTransition = doing(new LogAction(logText))


    def goTo(destinationState: T): DefiningState = {
      sourceStates.foreach(sourceState => builder.addTransition(new Transition[T, E, P](sourceState, destinationState, conditions, priority, actions)))
      new DefiningState(sourceStates)
    }

    def withPrio(priority: P): DefiningTransition = {
      this.priority = priority
      this
    }
  }

  def build(): StateMachine[T, E, P] = builder.build()

  def state(state: T): DefiningState = states(state)

  def states(states: T*): DefiningState = new DefiningState(mutable.HashSet[T](states: _*))
}

