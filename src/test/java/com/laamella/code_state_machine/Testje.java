package com.laamella.code_state_machine;

import org.junit.Test;

public class Testje {
	public enum Game {
		INTRO, MENU, CONFIGURATION, GET_READY, LEVEL, LEVEL_FINISH, GAME_OVER
	}

	@Test
	public void setUpGameMachine() {
		new Machine<Game>();
	}
}
