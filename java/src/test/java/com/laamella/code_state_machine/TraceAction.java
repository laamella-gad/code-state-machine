package com.laamella.code_state_machine;

/**
 * Action that appends some text to a stringbuffer, so the stringbuffer contains
 * a log of actions.
 */
public class TraceAction implements Action {
	private final StringBuffer trace;
	private final String signature;

	public TraceAction(final StringBuffer trace, final String signature) {
		this.trace = trace;
		this.signature = signature;
	}

	@Override
	public void execute() {
		trace.append(signature);
	}
}
