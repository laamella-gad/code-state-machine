package com.laamella.code_state_machine.action;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Condition;

/**
 * An action which finishes at some time in the future. A transition can wait
 * for the action to be finished by using the isFinished condition.
 */
public interface FinishableAction<E> extends Action {
	Condition<E> isFinished();
}
