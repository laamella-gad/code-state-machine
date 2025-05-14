package com.laamella.kode_state_machine;

import com.laamella.kode_state_machine.builder.DslStateMachineBuilder;
import com.laamella.kode_state_machine.io.DotOutput;
import com.laamella.kode_state_machine.priority.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.laamella.kode_state_machine.GameEvent.*;
import static com.laamella.kode_state_machine.GameState.*;
import static com.laamella.kode_state_machine.StateMachineAssert.assertActive;
import static com.laamella.kode_state_machine.priority.Priority.NORMAL;

public class StateTests {
    private static final Logger log = LoggerFactory.getLogger(StateTests.class);

    private StateMachine<GameState, GameEvent, Priority> gameMachine;

    @BeforeEach
    public void before() {
        var gameMachineBuilder = new DslStateMachineBuilder<GameState, GameEvent, Priority>(NORMAL) {
            @Override
            protected void executeBuildInstructions() {
                state(LOADER).onExit(log("exit!")).onEntry(log("enter!"));

                state(LOADER).isAStartState().when(DONE).action(log("bing!")).then(INTRO);
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
        };
        gameMachine = gameMachineBuilder.build();
        log.trace("\n" + new DotOutput<GameState, GameEvent, Priority>().getOutput(gameMachine));
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
