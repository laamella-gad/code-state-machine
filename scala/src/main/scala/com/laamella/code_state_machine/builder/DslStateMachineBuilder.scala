package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine._
import com.laamella.code_state_machine.action.LogAction
import com.laamella.code_state_machine.condition.{AfterCondition, AlwaysCondition, MultiEventMatchCondition, NeverCondition, SingleEventMatchCondition, StatesActiveCondition, StatesInactiveCondition}

import scala.collection.mutable

/**
 * A pretty "DSL" builder for a state machine.
 */
abstract class DslStateMachineBuilder[T, E, P <: Ordered[P]](defaultPriority: P) extends StateMachineBuilder[T, E, P] {

  class DefiningState(sourceStates: mutable.Set[T], internals: StateMachine[T, E, P]#Internals) {
    //		private Set<T> sourceStates = new HashSet<T>();
    //		private final StateMachine<T, E, P>.Internals internals;
    //
    //		public DefiningState() {
    //			this.sourceStates = sourceStates;
    //			this.internals = internals;
    //		}

    def except(states: T*): DefiningState = {
      for (state <- states) {
        sourceStates.remove(state)
      }
      this
    }

    def onExit(action: (() => Unit)*): DefiningState = {
      for (sourceState <- sourceStates) {
        internals.addExitActions(sourceState, action)
      }
      this
    }

    def onEntry(action: (() => Unit)*): DefiningState = {
      for (sourceState <- sourceStates) {
//        debug(s"Create entry action for $sourceState $action")
        internals.addEntryActions(sourceState, action)
      }
      this
    }

    def isAnEndState(): DefiningState = {
      for (state <- sourceStates) {
        internals.addEndState(state)
      }
      this
    }

    def isAStartState(): DefiningState = {
      for (state <- sourceStates) {
        internals.addStartState(state)
      }
      this
    }

    def areEndStates(): DefiningState = {
      isAnEndState()
    }

    def areStartStates(): DefiningState = isAStartState()

    def whenConditions(condition: Condition[E]*): DefiningTransition = {
      new DefiningTransition(sourceStates, new Conditions[E](condition: _*), internals)
    }

    def whenEvents(events: E*): DefiningTransition = {
      new DefiningTransition(sourceStates, is(events:_*), internals)
    }

  }

  class DefiningTransition(sourceStates: mutable.Set[T], conditions: Conditions[E], internals: StateMachine[T, E, P]#Internals) {
    private val actions = mutable.MutableList[() => Unit]()
    private var priority = defaultPriority

    // TODO
    //		public DefiningTransition() {
    //			this.sourceStates = sourceStates;
    //			this.conditions = conditions;
    //			this.internals = internals;
    //		}

    def action(action: () => Unit): DefiningTransition = {
      actions +=action
      this
    }

    def then(destinationState: T): DefiningState = {
      transition(destinationState, conditions, priority, actions)
    }

    def transition(destinationState: T, storedConditions2: Conditions[E], priority: P, actions: Seq[() => Unit]): DefiningState = {
      this.actions ++= actions
      for (sourceState <- sourceStates) {
        internals.addTransition(new Transition[T, E, P](sourceState, destinationState, storedConditions2, priority, this.actions))
      }
      new DefiningState(sourceStates, internals)
    }

    def transition(destinationState: T, condition: Condition[E], priority: P, actions: (() => Unit)*): DefiningState = {
      transition(destinationState, new Conditions[E](condition), priority, actions)
    }

    def withPrio(priority: P): DefiningTransition = {
      this.priority = priority
      this
    }
  }

  private var machine: StateMachine[T, E, P] = _

  override def build(newMachine: StateMachine[T, E, P]): StateMachine[T, E, P] = {
    machine = newMachine
    executeBuildInstructions()
    machine
  }

  // TODO see if we can change this pattern to get rid of the var machine
  protected def executeBuildInstructions(): Unit

  override def build(): StateMachine[T, E, P] = {
    build(new StateMachine[T, E, P]())
  }

  def state(state: T): DefiningState = {
    states(state)
  }

  def states(states: T*): DefiningState = {
    val m = machine
    new DefiningState(mutable.HashSet[T](states: _*), new m.Internals())
  }

  def active(statesThatMustBeActive: T*): Condition[E] = {
    new StatesActiveCondition[T, E, P](machine, statesThatMustBeActive: _*)
  }

  def inactive(statesThatMustBeInactive: T*): Condition[E] = {
    new StatesInactiveCondition[T, E, P](machine, statesThatMustBeInactive: _*)
  }

  def always() = new AlwaysCondition[E]()

  def never() = new NeverCondition[E]()

  def after(milliseconds: Long) = new AfterCondition[E](milliseconds)

  def is(events: E*): Conditions[E] = {
    if (events.length == 1) {
      val singleEvent = events(0)
      return new Conditions[E](new SingleEventMatchCondition[E](singleEvent))
    }
    new Conditions[E](new MultiEventMatchCondition[E](events: _*))
  }

  def log(logText: String): () => Unit = {
    new LogAction(logText)
  }

}

