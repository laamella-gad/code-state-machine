package com.laamella.code_state_machine;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static com.laamella.code_state_machine.GameEvent.*;
import static com.laamella.code_state_machine.GameState.*;

public class Testje {
	private static class GameMachineBuilder extends
			Machine.Builder<GameState, GameEvent> {
		public GameMachineBuilder() {
			super(LOADER);

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
			state(LOADER).when(DONE).action(bing).then(INTRO);
			state(INTRO).when(DONE).then(MENU);
			state(MENU).when(START).then(GET_READY);
			state(GET_READY).when(DONE).then(LEVEL);
			state(LEVEL_FINISH).when(DONE).then(GET_READY);
			state(LEVEL).when(DEAD).then(GAME_OVER).when(COMPLETE).then(
					LEVEL_FINISH);
			state(GAME_OVER).when(DONE).then(MENU);
			states(GameState.values()).when(ESCAPE).then(MENU);

			state(MENU).when(FIRE_A, FIRE_B).then(CONFIGURATION);
			state(CONFIGURATION).when(FIRE_A, FIRE_B).then(MENU);

			state(CONFIGURATION).when(FIRE_A).then(INTRO);
		}
	}

	private Machine<GameState, GameEvent> gameMachine;

	@Before
	public void before() {
		gameMachine = new GameMachineBuilder().buildMachine();

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
	public void reset() {
		gameMachine.handleEvent(GameEvent.DONE);
		gameMachine.reset();
		assertActive(gameMachine, GameState.LOADER);
	}

	private static <T extends Enum<?>, E> void assertActive(
			final Machine<T, E> machine, final T... expectedStates) {
		for (final T expectedState : expectedStates) {
			if (!machine.isActive(expectedState)) {
				fail("Expected " + expectedState + " to be active.");
			}
		}
		final Set<T> expectedStatesSet = new HashSet<T>(Arrays
				.asList(expectedStates));
		for (final T actualState : machine.getActiveStates()) {
			if (!expectedStatesSet.contains(actualState)) {
				fail("" + actualState + " was active, but not expected.");
			}
		}
	}

}
