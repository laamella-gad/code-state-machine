package com.laamella.code_state_machine;

public interface Precondition<E> {
	boolean isMet(E event);

}
