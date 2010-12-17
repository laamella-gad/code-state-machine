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
import static com.laamella.code_state_machine.StateMachineAssert.assertActive;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.action.NoAction;
import com.laamella.code_state_machine.builder.DslStateMachineBuilder;
import com.laamella.code_state_machine.io.DotOutput;
import com.laamella.code_state_machine.priority.Priority;

public class StateTests {
	private static final Logger log = LoggerFactory.getLogger(StateTests.class);

	private static class GameMachineBuilder extends DslStateMachineBuilder<GameState, GameEvent, Priority> {
		public GameMachineBuilder() {
			super(Priority.NORMAL);
			state(LOADER).onExit(NoAction.INSTANCE).onEntry(NoAction.INSTANCE);

			final Action bing = NoAction.INSTANCE;
			state(LOADER).isAStartState().when(DONE).action(bing).then(INTRO);
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

			state(EXIT).isAnEndState();
		}
	}

	private StateMachine<GameState, GameEvent, Priority> gameMachine;

	@Before
	public void before() {
		gameMachine = new GameMachineBuilder().buildMachine();
		log.trace("\n" + new DotOutput<GameState, GameEvent, Priority>(gameMachine.getMetaInformation()).getOutput());
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
}
