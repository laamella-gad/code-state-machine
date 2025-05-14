package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.Action
import com.laamella.kode_state_machine.Actions
import com.laamella.kode_state_machine.Condition
import com.laamella.kode_state_machine.Conditions
import com.laamella.kode_state_machine.StateMachine
import com.laamella.kode_state_machine.Transition
import com.laamella.kode_state_machine.action.LogAction
import com.laamella.kode_state_machine.condition.AfterCondition
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.condition.MultiEventMatchCondition
import com.laamella.kode_state_machine.condition.NeverCondition
import com.laamella.kode_state_machine.condition.SingleEventMatchCondition
import com.laamella.kode_state_machine.condition.StatesActiveCondition
import com.laamella.kode_state_machine.condition.StatesInactiveCondition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * A pretty "DSL" builder for a state machine.
 */
abstract class DslStateMachineBuilder<T, E, P : Comparable<P>>(private val defaultPriority: P) :
    StateMachineBuilder<T, E, P> {
    inner class DefiningState(
        private val sourceStates: MutableSet<T>,
        private val internals: StateMachine<T, E, P>.Internals
    ) {

        fun except(vararg states: T): DefiningState {
            for (state in states) {
                sourceStates.remove(state)
            }
            return this
        }

        fun onExit(vararg action: Action): DefiningState {
            for (sourceState in sourceStates) {
                internals.addExitActions(sourceState, *action)
            }
            return this
        }

        fun onEntry(vararg action: Action): DefiningState {
            for (sourceState in sourceStates) {
                log.debug("Create entry action for {} ({})", sourceState, action)
                internals.addEntryActions(sourceState, *action)
            }
            return this
        }

        val isAnEndState: DefiningState
            get() {
                for (state in sourceStates) {
                    internals.addEndState(state)
                }
                return this
            }

        val isAStartState: DefiningState
            get() {
                for (state in sourceStates) {
                    internals.addStartState(state)
                }
                return this
            }

        fun areEndStates(): DefiningState {
            return this.isAnEndState
        }

        fun areStartStates(): DefiningState {
            return this.isAStartState
        }

        fun `when`(vararg condition: Condition<E>): DefiningTransition {
            return DefiningTransition(sourceStates, Conditions(*condition), internals)
        }

        // This method exists only to suppress warnings about varargs.
        fun `when`(condition: Condition<E>): DefiningTransition {
            return DefiningTransition(sourceStates, Conditions(condition), internals)
        }

        fun `when`(vararg events: E): DefiningTransition {
            return DefiningTransition(sourceStates, `is`(*events), internals)
        }
    }

    inner class DefiningTransition(
        private val sourceStates: MutableSet<T>,
        private val conditions: Conditions<E>,
        private val internals: StateMachine<T, E, P>.Internals
    ) {
        private val actions = Actions()
        private var priority: P = defaultPriority

        fun action(action: Action): DefiningTransition {
            this.actions.add(action)
            return this
        }

        fun then(destinationState: T): DefiningState {
            return transition(destinationState, conditions, priority, actions)
        }

        fun transition(
            destinationState: T,
            storedConditions2: Conditions<E>,
            priority: P,
            actions: Actions
        ): DefiningState {
            this.actions.add(actions)
            for (sourceState in sourceStates) {
                internals.addTransition(
                    Transition(
                        sourceState,
                        destinationState,
                        storedConditions2,
                        priority,
                        this.actions
                    )
                )
            }
            return DefiningState(sourceStates, internals)
        }

        fun transition(
            destinationState: T,
            condition: Condition<E>,
            priority: P,
            vararg actions: Action
        ): DefiningState {
            return transition(destinationState, Conditions<E>(condition), priority, Actions(*actions))
        }

        fun withPrio(priority: P): DefiningTransition {
            this.priority = priority
            return this
        }
    }

    private var machine: StateMachine<T, E, P>? = null

    override fun build(newMachine: StateMachine<T, E, P>): StateMachine<T, E, P> {
        machine = newMachine
        executeBuildInstructions()
        return machine!!
    }

    protected abstract fun executeBuildInstructions()

    override fun build(): StateMachine<T, E, P> {
        return build(StateMachine())
    }

    fun state(state: T): DefiningState {
        return states(state)
    }

    fun states(vararg states: T): DefiningState {
        return DefiningState(HashSet(Arrays.asList(*states)), machine!!.Internals())
    }

    fun active(vararg statesThatMustBeActive: T): Condition<E> {
        return StatesActiveCondition(machine!!, *statesThatMustBeActive)
    }

    fun inactive(vararg statesThatMustBeInactive: T): Condition<E> {
        return StatesInactiveCondition(machine!!, *statesThatMustBeInactive)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DslStateMachineBuilder::class.java)

        fun <E> always(): Condition<E> {
            return AlwaysCondition()
        }

        fun <E> never(): Condition<E> {
            return NeverCondition()
        }

        fun <E> after(milliseconds: Long): Condition<E> {
            return AfterCondition(milliseconds)
        }

        fun <E> `is`(vararg events: E): Conditions<E> {
            assert(events.isNotEmpty())

            if (events.size == 1) {
                val singleEvent: E = events[0]
                return Conditions(SingleEventMatchCondition<E>(singleEvent))
            }

            return Conditions(MultiEventMatchCondition<E>(*events))
        }

        fun log(logText: String): Action {
            return LogAction(logText)
        }
    }
}
