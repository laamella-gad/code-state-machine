package com.laamella.code_state_machine;

public interface Action<E> {
	void execute(E event);
}
