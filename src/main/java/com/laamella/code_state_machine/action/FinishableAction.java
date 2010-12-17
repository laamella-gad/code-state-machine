package com.laamella.code_state_machine.action;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;

/**
 * An action which finishes at some time in the future. A transition can wait
 * for the action to be finished by using the isFinished precondition.
 */
public interface FinishableAction<E> extends Action {
	Precondition<E> isFinished();
}
