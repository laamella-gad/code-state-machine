package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine._
import com.laamella.code_state_machine.action.LogAction

import scala.collection.mutable

/**
  * A pretty "DSL" builder for a state machine.
  *
  * @tparam State State type. Each state should have a single instance of this type.
  *           An enum is a good fit.
  * @tparam Event Event type. Events come into the state machine from the outside
  *           world, and are used to trigger state transitions.
  * @tparam Priority Priority type. Will be used to give priorities to transitions.
  *           Enums and Integers are useful here.
  */
class DslStateMachineBuilder[State, Event, Priority <: Ordered[Priority]](defaultPriority: Priority) {
  private val builder = new StateMachineBuilder[State, Event, Priority]()

  class DefiningState(sourceStates: mutable.Set[State]) {
    def except(states: State*): DefiningState = {
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

    def isAnEndState: DefiningState = {
      sourceStates.foreach(builder.addEndState)
      this
    }

    def isAStartState: DefiningState = {
      sourceStates.foreach(builder.addStartState)
      this
    }

    def areEndStates: DefiningState = isAnEndState

    def areStartStates: DefiningState = isAStartState

    def when(conditions: Condition[Event]*): DefiningTransition = new DefiningTransition(sourceStates, conditions)

    def always: DefiningTransition = new DefiningTransition(sourceStates, Seq(new AlwaysCondition[Event]()))

    def never: DefiningTransition = new DefiningTransition(sourceStates, Seq(new NeverCondition[Event]()))

    def after(milliseconds: Long): DefiningTransition = new DefiningTransition(sourceStates, Seq(new AfterCondition[Event](milliseconds)))

    def onEvents(events: Event*): DefiningTransition = {
      val condition = if (events.length == 1) {
        val singleEvent = events(0)
        new SingleEventMatchCondition[Event](singleEvent)
      } else {
        new MultiEventMatchCondition[Event](events: _*)
      }
      new DefiningTransition(sourceStates, Seq(condition))
    }

    // FIXME find a way to rescue these conditions
//      def active(statesThatMustBeActive: State*): Condition[Event] = {
//        new StatesActiveCondition[State, Event, Priority](machine, statesThatMustBeActive: _*)
//      }
    //
//      def inactive(statesThatMustBeInactive: State*): Condition[Event] = {
//        new StatesInactiveCondition[State, Event, Priority](machine, statesThatMustBeInactive: _*)
//      }

  }

  class DefiningTransition(sourceStates: mutable.Set[State], conditions: Seq[Condition[Event]]) {
    private val actions = mutable.MutableList[() => Unit]()
    private var priority = defaultPriority

    def doing(action: () => Unit): DefiningTransition = {
      actions += action
      this
    }

    def log(logText: String): DefiningTransition = doing(new LogAction(logText))


    def goTo(destinationState: State): DefiningState = {
      sourceStates.foreach(sourceState => builder.addTransition(new Transition[State, Event, Priority](sourceState, destinationState, conditions, priority, actions)))
      new DefiningState(sourceStates)
    }

    def withPrio(priority: Priority): DefiningTransition = {
      this.priority = priority
      this
    }
  }

  def build(): StateMachine[State, Event, Priority] = builder.build()

  def state(state: State): DefiningState = states(state)

  def states(states: State*): DefiningState = new DefiningState(mutable.HashSet[State](states: _*))
}

