package com.laamella.code_state_machine.action

import com.laamella.code_state_machine.Action
import grizzled.slf4j.Logging

/**
 * This action logs a line.
 */
final class LogAction(logText: String) extends Action with Logging {
  override def execute() = trace(logText)

  override def toString = s"log ($logText)"
}