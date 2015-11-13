package com.laamella.code_state_machine.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Actions;
import com.laamella.code_state_machine.Condition;
import com.laamella.code_state_machine.Conditions;
import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.Transition;
import com.laamella.code_state_machine.action.LogAction;
import com.laamella.code_state_machine.condition.AfterCondition;
import com.laamella.code_state_machine.condition.AlwaysCondition;
import com.laamella.code_state_machine.condition.MultiEventMatchCondition;
import com.laamella.code_state_machine.condition.NeverCondition;
import com.laamella.code_state_machine.condition.SingleEventMatchCondition;
import com.laamella.code_state_machine.condition.StatesActiveCondition;
import com.laamella.code_state_machine.condition.StatesInactiveCondition;

/**
 * A pretty "DSL" builder for a state machine.
 */
public abstract class DslStateMachineBuilder<T, E, P extends Comparable<P>> implements StateMachineBuilder<T, E, P> {
	private static final Logger log = LoggerFactory.getLogger(DslStateMachineBuilder.class);

	public class DefiningState {
		private Set<T> sourceStates = new HashSet<T>();
		private final StateMachine<T, E, P>.Internals internals;

		public DefiningState(final Set<T> sourceStates, final StateMachine<T, E, P>.Internals internals) {
			this.sourceStates = sourceStates;
			this.internals = internals;
		}

		public DefiningState except(final T... states) {
			for (final T state : states) {
				sourceStates.remove(state);
			}
			return this;
		}

		public DefiningState onExit(final Action... action) {
			for (final T sourceState : sourceStates) {
				internals.addExitActions(sourceState, action);
			}
			return this;
		}

		public DefiningState onEntry(final Action... action) {
			for (final T sourceState : sourceStates) {
				log.debug("Create entry action for {} ({})", sourceState, action);
				internals.addEntryActions(sourceState, action);
			}
			return this;
		}

		public DefiningState isAnEndState() {
			for (final T state : sourceStates) {
				internals.addEndState(state);
			}
			return this;
		}

		public DefiningState isAStartState() {
			for (final T state : sourceStates) {
				internals.addStartState(state);
			}
			return this;
		}

		public DefiningState areEndStates() {
			return isAnEndState();
		}

		public DefiningState areStartStates() {
			return isAStartState();
		}

		public DefiningTransition when(final Condition<E>... condition) {
			assert condition != null;
			return new DefiningTransition(sourceStates, new Conditions<E>(condition), internals);
		}

		// This method exists only to suppress warnings about varargs.
		public DefiningTransition when(final Condition<E> condition) {
			assert condition != null;
			return new DefiningTransition(sourceStates, new Conditions<E>(condition), internals);
		}

		public DefiningTransition when(final E... events) {
			return new DefiningTransition(sourceStates, is(events), internals);
		}

	}

	public class DefiningTransition {
		private final Conditions<E> conditions;
		private final Actions actions = new Actions();
		private final Set<T> sourceStates;
		private P priority = defaultPriority;
		private final StateMachine<T, E, P>.Internals internals;

		public DefiningTransition(final Set<T> sourceStates, final Conditions<E> conditions,
				final StateMachine<T, E, P>.Internals internals) {
			this.sourceStates = sourceStates;
			this.conditions = conditions;
			this.internals = internals;
		}

		public DefiningTransition action(final Action action) {
			assert action != null;
			this.actions.add(action);
			return this;
		}

		public DefiningState then(final T destinationState) {
			assert destinationState != null;
			return transition(destinationState, conditions, priority, actions);
		}

		public DefiningState transition(final T destinationState, final Conditions<E> storedConditions2,
				final P priority, final Actions actions) {
			this.actions.add(actions);
			for (final T sourceState : sourceStates) {
				internals.addTransition(new Transition<T, E, P>(sourceState, destinationState, storedConditions2,
						priority, this.actions));
			}
			return new DefiningState(sourceStates, internals);
		}

		public DefiningState transition(final T destinationState, final Condition<E> condition, final P priority,
				final Action... actions) {
			return transition(destinationState, new Conditions<E>(condition), priority, new Actions(actions));
		}

		public DefiningTransition withPrio(final P priority) {
			assert priority != null;
			this.priority = priority;
			return this;
		}
	}

	private StateMachine<T, E, P> machine;
	private final P defaultPriority;

	public DslStateMachineBuilder(final P defaultPriority) {
		assert defaultPriority != null;
		this.defaultPriority = defaultPriority;
	}

	@Override
	public StateMachine<T, E, P> build(final StateMachine<T, E, P> newMachine) {
		assert newMachine != null;
		machine = newMachine;
		executeBuildInstructions();
		return machine;
	}

	protected abstract void executeBuildInstructions();

	@Override
	public StateMachine<T, E, P> build() {
		return build(new StateMachine<T, E, P>());
	}

	@SuppressWarnings("unchecked")
	public DefiningState state(final T state) {
		assert state != null;
		return states(state);
	}

	public DefiningState states(final T... states) {
		return new DefiningState(new HashSet<T>(Arrays.asList(states)), machine.new Internals());
	}

	public static <E> Condition<E> always() {
		return new AlwaysCondition<E>();
	}

	public static <E> Condition<E> never() {
		return new NeverCondition<E>();
	}

	public static <E> Condition<E> after(final long milliseconds) {
		return new AfterCondition<E>(milliseconds);
	}

	public static <E> Conditions<E> is(final E... events) {
		assert events != null;
		assert events.length != 0;

		if (events.length == 1) {
			final E singleEvent = events[0];
			return new Conditions<E>(new SingleEventMatchCondition<E>(singleEvent));
		}

		return new Conditions<E>(new MultiEventMatchCondition<E>(events));
	}

	public Condition<E> active(final T... statesThatMustBeActive) {
		return new StatesActiveCondition<T, E, P>(machine, statesThatMustBeActive);
	}

	public Condition<E> inactive(final T... statesThatMustBeInactive) {
		return new StatesInactiveCondition<T, E, P>(machine, statesThatMustBeInactive);
	}

	public static Action log(final String logText) {
		assert logText != null;
		return new LogAction(logText);
	}

}
