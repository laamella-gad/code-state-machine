package com.laamella.code_state_machine;

public class TraceAction implements Action {
	private final StringBuffer trace;
	private final String signature;

	public TraceAction(StringBuffer trace, String signature) {
		this.trace = trace;
		this.signature = signature;
	}

	@Override
	public void execute() {
		trace.append(signature);
	}
}
