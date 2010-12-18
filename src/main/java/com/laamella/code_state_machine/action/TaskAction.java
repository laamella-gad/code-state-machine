package com.laamella.code_state_machine.action;

import java.lang.Thread.State;

import com.laamella.code_state_machine.Condition;
import com.laamella.code_state_machine.condition.NonEventBasedCondition;

/**
 * This action starts a separate work thread with user code. A transition can
 * wait for this work to be finished by using the isFinished condition.
 * 
 * @param <E>
 *            event type.
 */
// TODO test
public abstract class TaskAction<E> implements Runnable, FinishableAction<E> {
	private Thread taskThread;
	private NonEventBasedCondition<E> finishedCondition;

	@Override
	public final void execute() {
		taskThread = new Thread(this);
		finishedCondition = new NonEventBasedCondition<E>() {
			@Override
			public boolean isMet() {
				return taskThread.getState() == State.TERMINATED;
			}
		};
		taskThread.start();
	}

	@Override
	public final Condition<E> isFinished() {
		return finishedCondition;
	}
}
