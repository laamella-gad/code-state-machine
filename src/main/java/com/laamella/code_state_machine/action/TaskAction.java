package com.laamella.code_state_machine.action;

import java.lang.Thread.State;

import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.precondition.NonEventBasedPrecondition;

/**
 * This action starts a separate work thread with user code. A transition can
 * wait for this work to be finished by using the isFinished precondition.
 * 
 * @param <E>
 *            event type.
 */
// TODO test
public abstract class TaskAction<E> implements Runnable, FinishableAction<E> {
	private Thread taskThread;
	private NonEventBasedPrecondition<E> finishedPrecondition;

	@Override
	public final void execute() {
		taskThread = new Thread(this);
		finishedPrecondition = new NonEventBasedPrecondition<E>() {
			@Override
			public boolean isMet() {
				return taskThread.getState() == State.TERMINATED;
			}
		};
		taskThread.start();
	}

	@Override
	public final Precondition<E> isFinished() {
		return finishedPrecondition;
	}
}
