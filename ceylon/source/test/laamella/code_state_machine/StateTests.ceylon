import ceylon.logging {
	logger,
	Logger,
	addLogWriter,
	Priority,
	Category,
	info,
	trace
}
import ceylon.test {
	...
}

import laamella.code_state_machine {
	StateMachine,
	LeveledPriority,
	normal
}
import laamella.code_state_machine.builders {
	DslStateMachineBuilder,
	log
}
import laamella.code_state_machine.io {
	DotOutput
}
import ceylon.time {
	now
}
import ceylon.locale {
	systemLocale
}

variable StateMachine<GameState,GameEvent,LeveledPriority> gameMachine = StateMachine<GameState,GameEvent,LeveledPriority>();
Logger logge = logger(`package test.laamella.code_state_machine`);

beforeTest
void initStateMachine() {
	addLogWriter {
		void log(Priority p, Category c, String m, Throwable? t) {
			value print = p <= info
					then process.write
					else process.writeError;
			value instant = now();
			value formats = systemLocale.formats;
			value date = formats.shortFormatDate(instant.date());
			value time = formats.mediumFormatTime(instant.time());
			print("[``date`` at ``time``] ``p.string``: ``m``");
			print(operatingSystem.newline);
			if (exists t) {
				printStackTrace(t, print);
			}
		}
	};
	logge.priority = trace;
	
	object gameMachineBuilder extends DslStateMachineBuilder<GameState,GameEvent,LeveledPriority>(gameMachine, normal) {
		shared actual void executeBuildInstructions() {
			state(loader).onExit(log("exit!")).onEntry(log("enter!"));
			
			state(loader).isAStartState().onEvent(done).action(log("bing!")).then_(intro);
			state(intro).onEvent(done).then_(menu);
			state(menu).onEvent(start).then_(getReady).onEvent(escape).then_(exit);
			state(getReady).onEvent(done).then_(level);
			state(levelFinish).onEvent(done).then_(getReady);
			state(level).onEvent(dead).then_(gameOver).onEvent(complete).then_(levelFinish);
			state(gameOver).onEvent(done).then_(menu);
			states(`GameState`.caseValues).except(menu, loader, exit).onEvent(escape).then_(menu);
			
			state(menu).onEvents({ fireA, fireB }).then_(configuration);
			state(configuration).onEvents({ fireA, fireB }).then_(menu);
			
			state(configuration).onEvent(fireA).then_(intro);
			
			state(exit).isAnEndState();
		}
	}
	gameMachine = gameMachineBuilder.build(gameMachine);
}

test
void dotOutput() {
	logge.trace("\n" + DotOutput<GameState,GameEvent,LeveledPriority>().getOutput(gameMachine));
}

//behavior of "a state machine"
//
//it should "support concurrent states" in {
//	gameMachine.handleEvent(DONE)
//	gameMachine.handleEvent(DONE)
//	gameMachine.handleEvent(FIRE_A)
//	gameMachine.handleEvent(FIRE_A)
//	assertActive(gameMachine, MENU, INTRO)
//	gameMachine.handleEvent(START)
//	assertActive(gameMachine, GET_READY, INTRO)
//	gameMachine.handleEvent(DONE)
//	assertActive(gameMachine, LEVEL, MENU)
//	gameMachine.handleEvent(START)
//	assertActive(gameMachine, LEVEL, GET_READY)
//	gameMachine.handleEvent(DONE)
//	assertActive(gameMachine, LEVEL)
//}
//
//it should "have LOADER as the start state" in {
//		assertActive(gameMachine, LOADER)
//	}
//	
//	it should "loading is done, the intro starts" in {
//		gameMachine.handleEvent(DONE)
//		assertActive(gameMachine, INTRO)
//	}
//	
//	it should "have no states left when we get to the end state" in {
//		assertActive(gameMachine, LOADER)
//		gameMachine.handleEvent(DONE)
//		assertActive(gameMachine, INTRO)
//		gameMachine.handleEvent(DONE)
//		assertActive(gameMachine, MENU)
//		gameMachine.handleEvent(ESCAPE)
//		assertActive(gameMachine)
//	}
//	
//	it should "reset to the start state correctly" in {
//		gameMachine.handleEvent(DONE)
//		gameMachine.reset()
//		assertActive(gameMachine, LOADER)
//	}
//}

test
void after() {
	assert (1 == 1);
}
