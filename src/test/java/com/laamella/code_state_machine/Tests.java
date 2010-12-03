package com.laamella.code_state_machine;

import static com.laamella.code_state_machine.GameEvent.COMPLETE;
import static com.laamella.code_state_machine.GameEvent.DEAD;
import static com.laamella.code_state_machine.GameEvent.DONE;
import static com.laamella.code_state_machine.GameEvent.ESCAPE;
import static com.laamella.code_state_machine.GameEvent.FIRE_A;
import static com.laamella.code_state_machine.GameEvent.FIRE_B;
import static com.laamella.code_state_machine.GameEvent.START;
import static com.laamella.code_state_machine.GameState.CONFIGURATION;
import static com.laamella.code_state_machine.GameState.EXIT;
import static com.laamella.code_state_machine.GameState.GAME_OVER;
import static com.laamella.code_state_machine.GameState.GET_READY;
import static com.laamella.code_state_machine.GameState.INTRO;
import static com.laamella.code_state_machine.GameState.LEVEL;
import static com.laamella.code_state_machine.GameState.LEVEL_FINISH;
import static com.laamella.code_state_machine.GameState.LOADER;
import static com.laamella.code_state_machine.GameState.MENU;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.util.DotOutput;
import com.laamella.code_state_machine.util.DslStateMachineBuilder;

public class Tests {
	private static final Logger log = LoggerFactory.getLogger(Tests.class);

	private static class GameMachineBuilder extends DslStateMachineBuilder<GameState, GameEvent> {
		public GameMachineBuilder() {
			state(LOADER).onExit(new Action<GameEvent>() {
				@Override
				public void execute(final GameEvent event) {
				}
			}).onEntry(new Action<GameEvent>() {
				@Override
				public void execute(final GameEvent event) {
				}
			});

			final Action<GameEvent> bing = new Action<GameEvent>() {
				@Override
				public void execute(final GameEvent event) {
				}
			};
			state(LOADER).isStartState().when(DONE).action(bing).then(INTRO);
			state(INTRO).when(DONE).then(MENU);
			state(MENU).when(START).then(GET_READY).when(ESCAPE).then(EXIT);
			state(GET_READY).when(DONE).then(LEVEL);
			state(LEVEL_FINISH).when(DONE).then(GET_READY);
			state(LEVEL).when(DEAD).then(GAME_OVER).when(COMPLETE).then(LEVEL_FINISH);
			state(GAME_OVER).when(DONE).then(MENU);
			states(GameState.values()).except(MENU, LOADER, EXIT).when(ESCAPE).then(MENU);

			state(MENU).when(FIRE_A, FIRE_B).then(CONFIGURATION);
			state(CONFIGURATION).when(FIRE_A, FIRE_B).then(MENU);

			state(CONFIGURATION).when(FIRE_A).then(INTRO);

			state(EXIT).isEndState();
		}
	}

	private StateMachine<GameState, GameEvent> gameMachine;

	@Before
	public void before() {
		gameMachine = new GameMachineBuilder().buildMachine();
		log.trace("\n" + new DotOutput<GameState, GameEvent>(gameMachine.getMetaInformation()).getOutput());
	}

	@Test
	public void concurrentStates() {
		gameMachine.handleEvent(DONE);
		gameMachine.handleEvent(DONE);
		gameMachine.handleEvent(FIRE_A);
		gameMachine.handleEvent(FIRE_A);
		assertActive(gameMachine, MENU, INTRO);
		gameMachine.handleEvent(START);
		assertActive(gameMachine, GET_READY, INTRO);
		gameMachine.handleEvent(DONE);
		assertActive(gameMachine, LEVEL, MENU);
		gameMachine.handleEvent(START);
		assertActive(gameMachine, LEVEL, GET_READY);
		gameMachine.handleEvent(DONE);
		assertActive(gameMachine, LEVEL);

	}

	@Test
	public void startStateIsLoader() {
		assertActive(gameMachine, GameState.LOADER);
	}

	@Test
	public void loadingDone() {
		gameMachine.handleEvent(GameEvent.DONE);
		assertActive(gameMachine, GameState.INTRO);
	}

	@Test
	public void endState() {
		assertActive(gameMachine, LOADER);
		gameMachine.handleEvent(DONE);
		assertActive(gameMachine, INTRO);
		gameMachine.handleEvent(DONE);
		assertActive(gameMachine, MENU);
		gameMachine.handleEvent(ESCAPE);
		assertActive(gameMachine);
	}

	@Test
	public void reset() {
		gameMachine.handleEvent(GameEvent.DONE);
		gameMachine.reset();
		assertActive(gameMachine, GameState.LOADER);
	}

	private static <T extends Enum<?>, E> void assertActive(final StateMachine<T, E> machine, final T... expectedStates) {
		for (final T expectedState : expectedStates) {
			if (!machine.isActive(expectedState)) {
				fail("Expected " + expectedState + " to be active.");
			}
		}
		final Set<T> expectedStatesSet = new HashSet<T>(Arrays.asList(expectedStates));
		for (final T actualState : machine.getActiveStates()) {
			if (!expectedStatesSet.contains(actualState)) {
				fail("" + actualState + " was active, but not expected.");
			}
		}
	}
}
