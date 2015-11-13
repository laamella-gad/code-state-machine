package com.laamella.code_state_machine.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laamella.code_state_machine.Action;

/**
 * This action logs a line.
 */
public final class LogAction implements Action {
	private static final Logger log = LoggerFactory.getLogger(LogAction.class);
	private final String logText;

	public LogAction(final String logText) {
		this.logText = logText;
	}

	@Override
	public void execute() {
		log.trace(logText);
	}

	@Override
	public String toString() {
		return "log (" + logText + ")";
	}
}