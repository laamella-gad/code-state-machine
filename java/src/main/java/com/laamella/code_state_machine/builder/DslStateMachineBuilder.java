package com.laamella.code_state_machine.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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

import static java.util.Objects.*;

/**
 * A pretty "DSL" builder for a state machine.
 */
public abstract class DslStateMachineBuilder<T, E, P extends Comparable<P>> implements StateMachineBuilder<T, E, P> {
    private static final Logger log = LoggerFactory.getLogger(DslStateMachineBuilder.class);

    public class DefiningState {
        private final Set<T> sourceStates;
        private final StateMachine<T, E, P>.Internals internals;

        public DefiningState(final Set<T> sourceStates, final StateMachine<T, E, P>.Internals internals) {
            this.sourceStates = sourceStates;
            this.internals = internals;
        }

        @SafeVarargs
        public final DefiningState except(final T... states) {
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

        @SafeVarargs
        public final DefiningTransition when(final Condition<E>... condition) {
            requireNonNull(condition);
            return new DefiningTransition(sourceStates, new Conditions<>(condition), internals);
        }

        // This method exists only to suppress warnings about varargs.
        public DefiningTransition when(final Condition<E> condition) {
            requireNonNull(condition);
            return new DefiningTransition(sourceStates, new Conditions<>(condition), internals);
        }

        @SafeVarargs
        public final DefiningTransition when(final E... events) {
            return new DefiningTransition(sourceStates, is(events), internals);
        }

    }

    public class DefiningTransition {
        private final Conditions<E> conditions;
        private final Actions actions = new Actions();
        private final Set<T> sourceStates;
        private P priority = defaultPriority;
        private final StateMachine<T, E, P>.Internals internals;

        public DefiningTransition(Set<T> sourceStates, Conditions<E> conditions, StateMachine<T, E, P>.Internals internals) {
            this.sourceStates = sourceStates;
            this.conditions = conditions;
            this.internals = internals;
        }

        public DefiningTransition action(Action action) {
            requireNonNull(action);
            this.actions.add(action);
            return this;
        }

        public DefiningState then(T destinationState) {
            requireNonNull(destinationState);
            return transition(destinationState, conditions, priority, actions);
        }

        public DefiningState transition(T destinationState, Conditions<E> storedConditions2, P priority, Actions actions) {
            this.actions.add(actions);
            for (var sourceState : sourceStates) {
                internals.addTransition(new Transition<>(sourceState, destinationState, storedConditions2, priority, this.actions));
            }
            return new DefiningState(sourceStates, internals);
        }

        public DefiningState transition(T destinationState, Condition<E> condition, P priority, Action... actions) {
            return transition(destinationState, new Conditions<>(condition), priority, new Actions(actions));
        }

        public DefiningTransition withPrio(final P priority) {
            requireNonNull(priority);
            this.priority = priority;
            return this;
        }
    }

    private StateMachine<T, E, P> machine;
    private final P defaultPriority;

    public DslStateMachineBuilder(P defaultPriority) {
        requireNonNull(defaultPriority);
        this.defaultPriority = defaultPriority;
    }

    @Override
    public StateMachine<T, E, P> build(StateMachine<T, E, P> newMachine) {
        Objects.requireNonNull(newMachine);
        machine = newMachine;
        executeBuildInstructions();
        return machine;
    }

    protected abstract void executeBuildInstructions();

    @Override
    public StateMachine<T, E, P> build() {
        return build(new StateMachine<>());
    }

    public DefiningState state(T state) {
        Objects.requireNonNull(state);
        return states(state);
    }

    @SafeVarargs
    public final DefiningState states(T... states) {
        return new DefiningState(new HashSet<>(Arrays.asList(states)), machine.new Internals());
    }

    public static <E> Condition<E> always() {
        return new AlwaysCondition<>();
    }

    public static <E> Condition<E> never() {
        return new NeverCondition<>();
    }

    public static <E> Condition<E> after(final long milliseconds) {
        return new AfterCondition<>(milliseconds);
    }

    @SafeVarargs
    public static <E> Conditions<E> is(final E... events) {
        Objects.requireNonNull(events);
        assert events.length != 0;

        if (events.length == 1) {
            var singleEvent = events[0];
            return new Conditions<>(new SingleEventMatchCondition<>(singleEvent));
        }

        return new Conditions<>(new MultiEventMatchCondition<>(events));
    }

    @SafeVarargs
    public final Condition<E> active(T... statesThatMustBeActive) {
        return new StatesActiveCondition<>(machine, statesThatMustBeActive);
    }

    @SafeVarargs
    public final Condition<E> inactive(T... statesThatMustBeInactive) {
        return new StatesInactiveCondition<>(machine, statesThatMustBeInactive);
    }

    public static Action log(final String logText) {
        Objects.requireNonNull(logText);
        return new LogAction(logText);
    }
}
