package com.laamella.code_state_machine

/**
 * Action that appends some text to a stringbuffer, so the stringbuffer contains
 * a log of actions.
 */
class TraceAction(trace: StringBuffer, signature: String) extends Action {
  override def execute() = trace.append(signature)
}
